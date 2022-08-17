package io.intino.master.builder.operations;

import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.magritte.builder.core.CompilationUnit;
import io.intino.magritte.builder.core.CompilerConfiguration;
import io.intino.magritte.builder.core.errorcollection.CompilationFailedException;
import io.intino.magritte.builder.core.operation.model.ModelOperation;
import io.intino.magritte.builder.model.Model;
import io.intino.magritte.builder.model.NodeImpl;
import io.intino.magritte.lang.model.Node;
import io.intino.master.builder.operations.codegeneration.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static io.intino.magritte.builder.compiler.shared.TaraBuildConstants.PRESENTABLE_MESSAGE;
import static io.intino.magritte.builder.utils.Format.*;
import static java.io.File.separator;

public class MasterNodeCodeGenerationOperation extends ModelOperation {
	private static final String DOT = ".";
	private static final String JAVA = ".java";
	private static final Logger LOG = Logger.getGlobal();
	private final CompilerConfiguration conf;
	private final Map<String, List<String>> outMap = new LinkedHashMap<>();
	private final File srcFolder;
	private final File genFolder;
	private final Template entityTemplate;
	private final Template structTemplate;

	public MasterNodeCodeGenerationOperation(CompilationUnit unit) {
		super(unit);
		this.conf = unit.configuration();
		this.srcFolder = conf.sourceDirectories().isEmpty() ? null : conf.sourceDirectories().get(0);
		this.genFolder = conf.getOutDirectory();
		this.entityTemplate = customize(new EntityTemplate());
		this.structTemplate = customize(new StructTemplate());
	}

	@Override
	public void call(Model model) throws CompilationFailedException {
		try {
			if (conf.isVerbose()) conf.out().println(prefix() + " Generating Entities...");
			createEntities(model);
			createStructs(model);
			createMaster(model);
			compilationUnit.addOutputItems(outMap);
			compilationUnit.compilationDifferentialCache().saveCache(model.components().stream().map(c -> ((NodeImpl) c).getHashCode()).collect(Collectors.toList()));
		} catch (Throwable e) {
			LOG.log(java.util.logging.Level.SEVERE, "Error during java className generation: " + e.getMessage(), e);
			throw new CompilationFailedException(compilationUnit.getPhase(), compilationUnit, e);
		}
	}

	private void createEntities(Model model) {
		final Map<String, Map<String, String>> outputs = createEntityClasses(model);
		fillOutMap(outputs);
		outputs.values().forEach(this::write);
	}

	private void createStructs(Model model) {
		final Map<String, Map<String, String>> outputs = createStructClasses(model);
		fillOutMap(outputs);
		outputs.values().forEach(this::write);
	}

	private void createMaster(Model model) {
		final Map<String, Map<String, String>> outputs = createMasterClass(model);
		fillOutMap(outputs);
		outputs.values().forEach(this::write);
	}

	private Map<String, Map<String, String>> createEntityClasses(Model model) {
		Map<String, Map<String, String>> outputs = new HashMap<>();
		model.components().stream()
				.filter(node -> node.type().equals("Entity") && ((NodeImpl) node).isDirty() && !((NodeImpl) node).isVirtual())
				.forEach(node -> renderEntityNode(outputs, node));
		return outputs;
	}

	private Map<String, Map<String, String>> createStructClasses(Model model) {
		Map<String, Map<String, String>> outputs = new HashMap<>();
		model.components().stream()
				.filter(node -> node.type().equals("Struct") && ((NodeImpl) node).isDirty() && !((NodeImpl) node).isVirtual())
				.forEach(node -> renderStructNode(outputs, node));
		return outputs;
	}

	private Map<String, Map<String, String>> createMasterClass(Model model) {
		FrameBuilder builder = new FrameBuilder("master").add("package", conf.workingPackage());
		builder.add("entity", model.components().stream().filter(c -> c.type().equals("Entity"))
				.map(c -> {
					final FrameBuilder b = new FrameBuilder("entity").add("name", c.name());
					if (c.isAbstract()) b.add("abstract");
					return b.toFrame();
				}).toArray());
		String qn = conf.workingPackage() + DOT + firstUpperCase().format(javaValidName().format("MasterClient").toString());
		String iQn = conf.workingPackage() + DOT + firstUpperCase().format(javaValidName().format("Master").toString());
		return Map.of(model.components().get(0).file(),
				Map.of(destination(qn), customize(new MasterClientTemplate()).render(builder.toFrame()),
						destination(iQn), customize(new MasterClientTemplate()).render(builder.add("interface").toFrame()))
		);
	}

	private void renderEntityNode(Map<String, Map<String, String>> map, Node node) {
		Map<String, Frame> frames = new EntityFrameCreator(conf).create(node);
		if (!map.containsKey(node.file())) map.put(node.file(), new LinkedHashMap<>());
		frames.forEach((path, frame) -> {
			String destination = entityDestination(path, frame);
			map.get(node.file()).put(destination, !isModified(node) && new File(destination).exists() ? "" : entityTemplate.render(frame));
		});
	}

	private void renderStructNode(Map<String, Map<String, String>> map, Node node) {
		Map<String, Frame> frames = new StructFrameCreator(conf).create(node);
		if (!map.containsKey(node.file())) map.put(node.file(), new LinkedHashMap<>());
		frames.forEach((path, frame) -> {
			String destination = destination(path);
			map.get(node.file()).put(destination, !isModified(node) && new File(destination).exists() ? "" : structTemplate.render(frame));
		});
	}

	private void write(Map<String, String> outputsMap) {
		outputsMap.forEach((key, value) -> {
			File file = new File(key);
			if (value.isEmpty() || isUnderSource(file) && file.exists()) return;
			file.getParentFile().mkdirs();
			write(file, value);
		});
	}

	private boolean isUnderSource(File file) {
		return file.getAbsolutePath().startsWith(srcFolder.getAbsolutePath());
	}

	private void write(File file, String text) {
		try {
			file.getParentFile().mkdirs();
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
			fileWriter.write(text);
			fileWriter.close();
		} catch (IOException e) {
			LOG.log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		}
	}

	private boolean isModified(Node node) {
		return compilationUnit.compilationDifferentialCache().isModified((NodeImpl) node);
	}

	private String destination(String path) {
		return new File(genFolder, path.replace(DOT, separator) + JAVA).getAbsolutePath();
	}

	private String entityDestination(String path, Frame frame) {
		return new File(frame.is("decorable") && !frame.is("abstract") ? srcFolder : genFolder, path.replace(DOT, separator) + JAVA).getAbsolutePath();

	}

	private void fillOutMap(Map<String, Map<String, String>> map) {
		for (Map.Entry<String, Map<String, String>> entry : map.entrySet())
			for (String out : entry.getValue().keySet()) if (!isUnderSource(new File(out))) put(entry.getKey(), out);
	}

	private void put(String key, String value) {
		if (!outMap.containsKey(key)) outMap.put(key, new ArrayList<>());
		outMap.get(key).add(value);
	}

	private String prefix() {
		return PRESENTABLE_MESSAGE + "[" + conf.getModule() + " - " + conf.model().outDsl() + "]";
	}
}
