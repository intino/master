package io.intino.master.builder.operations;

import io.intino.itrules.Frame;
import io.intino.itrules.Template;
import io.intino.magritte.builder.core.CompilationUnit;
import io.intino.magritte.builder.core.CompilerConfiguration;
import io.intino.magritte.builder.core.errorcollection.CompilationFailedException;
import io.intino.magritte.builder.core.operation.model.ModelOperation;
import io.intino.magritte.builder.model.Model;
import io.intino.magritte.builder.model.NodeImpl;
import io.intino.magritte.builder.utils.Format;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Tag;
import io.intino.master.builder.operations.codegeneration.EntityFrameCreator;
import io.intino.master.builder.operations.codegeneration.EntityTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static io.intino.magritte.compiler.shared.TaraBuildConstants.PRESENTABLE_MESSAGE;
import static java.io.File.separator;

public class MasterCodeServerGenerationOperation extends ModelOperation {
	private static final String DOT = ".";
	private static final String JAVA = ".java";
	private static final Logger LOG = Logger.getGlobal();
	private final CompilerConfiguration conf;
	private final Map<String, List<String>> outMap = new LinkedHashMap<>();
	private final File srcFolder;
	private final File outFolder;
	private final Template entityTemplate;


	public MasterCodeServerGenerationOperation(CompilationUnit unit) {
		super(unit);
		this.conf = unit.configuration();
		this.srcFolder = conf.sourceDirectories().isEmpty() ? null : conf.sourceDirectories().get(0);
		this.outFolder = conf.getOutDirectory();
		this.entityTemplate = Format.customize(new EntityTemplate());
	}

	@Override
	public void call(Model model) throws CompilationFailedException {
		try {
			if (conf.isVerbose()) conf.out().println(prefix() + " Generating Entities...");
			createEntities(model);
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
		outputs.values().forEach(this::writeEntities);
	}

	private Map<String, Map<String, String>> createEntityClasses(Model model) {
		Map<String, Map<String, String>> outputs = new HashMap<>();
		model.components().stream()
				.filter(node -> !node.is(Tag.Instance) && ((NodeImpl) node).isDirty() && !((NodeImpl) node).isVirtual())
				.forEach(node -> renderNode(outputs, model, node));
		return outputs;
	}

	private void renderNode(Map<String, Map<String, String>> map, Model model, Node node) {
		Map.Entry<String, Frame> entityFrame = new EntityFrameCreator(conf, node.languageName(), model).create(node);
		if (!map.containsKey(node.file())) map.put(node.file(), new LinkedHashMap<>());
		String destination = destination(entityFrame);
		map.get(node.file()).put(destination, !isModified(node) && new File(destination).exists() ? "" : entityTemplate.render(entityFrame.getValue()));
	}

	private void writeEntities(Map<String, String> layersMap) {
		for (Map.Entry<String, String> entry : layersMap.entrySet()) {
			File file = new File(entry.getKey());
			if (entry.getValue().isEmpty() || isUnderSource(file) && file.exists()) continue;
			file.getParentFile().mkdirs();
			write(file, entry.getValue());
		}
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

	private String destination(Map.Entry<String, Frame> layerFrameMap) {
		return new File(outFolder, layerFrameMap.getKey().replace(DOT, separator) + JAVA).getAbsolutePath();
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
