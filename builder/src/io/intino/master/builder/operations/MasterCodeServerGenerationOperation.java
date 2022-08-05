package io.intino.master.builder.operations;

import io.intino.itrules.Frame;
import io.intino.magritte.builder.core.CompilationUnit;
import io.intino.magritte.builder.core.CompilerConfiguration;
import io.intino.magritte.builder.core.errorcollection.CompilationFailedException;
import io.intino.magritte.builder.core.operation.model.ModelOperation;
import io.intino.magritte.builder.model.Model;
import io.intino.magritte.builder.model.NodeImpl;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Tag;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static io.intino.magritte.compiler.shared.TaraBuildConstants.PRESENTABLE_MESSAGE;

public class MasterCodeServerGenerationOperation extends ModelOperation {
	private final CompilerConfiguration conf;
	private static final Logger LOG = Logger.getGlobal();
	private final Map<String, List<String>> outMap = new LinkedHashMap<>();
	private final File srcFolder;


	public MasterCodeServerGenerationOperation(CompilationUnit unit) {
		super(unit);
		this.conf = unit.configuration();
		this.srcFolder = conf.sourceDirectories().isEmpty() ? null : conf.sourceDirectories().get(0);

	}

	@Override
	public void call(Model model) throws CompilationFailedException {
		try {
			if (conf.isVerbose()) conf.out().println(prefix() + " Generating Layers...");
			createEntities(model);
			compilationUnit.addOutputItems(outMap);
			compilationUnit.compilationDifferentialCache().saveCache(model.components().stream().map(c -> ((NodeImpl) c).getHashCode()).collect(Collectors.toList()));
		} catch (Throwable e) {
			LOG.log(java.util.logging.Level.SEVERE, "Error during java className generation: " + e.getMessage(), e);
			throw new CompilationFailedException(compilationUnit.getPhase(), compilationUnit, e);
		}
	}

	private void createEntities(Model model) {
		createEntityClasses(model).values().forEach(this::writeEntities);
	}

	private Map<String, Map<String, String>> createEntityClasses(Model model) {
		Map<String, Map<String, String>> map = new HashMap<>();
		for (Node node : model.components()) {
			if (node.is(Tag.Instance) || !((NodeImpl) node).isDirty() || ((NodeImpl) node).isVirtual()) continue;
			renderNode(map, model, node);
		}
		return map;
	}

	private void renderNode(Map<String, Map<String, String>> map, Model model, Node node) {
		Map.Entry<String, Frame> layerFrame = new LayerFrameCreator(conf, node.languageName(), model).create(node);
		if (!map.containsKey(node.file())) map.put(node.file(), new LinkedHashMap<>());
		String destination = destination(layerFrame);
		map.get(node.file()).put(destination, !isModified(node) && new File(destination).exists() ? "" : render(layerFrame));
		renderFrame(map, node, model, layerFrame);
	}


	private void renderFrame(Map<String, Map<String, String>> map, Node node, Model model, Map.Entry<String, Frame> layerFrame) {
		if (node.is(Tag.Decorable)) {
			Map.Entry<String, Frame> frame = new LayerFrameCreator(conf, node.languageName(), model).createDecorable(node);
			File file = new File(srcDestiny(frame));
			if (file.exists() && node.isAbstract()) checkAbstractDecorable(file);
			map.get(node.file()).put(file.getAbsolutePath(), file.exists() ? "" : render(frame));
		} else removeDecorable(layerFrame.getKey(), node.name());
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



	private void registerOutputs(Map<String, String> nativeOuts) {
		for (Map.Entry<String, String> src : nativeOuts.entrySet()) {
			if (!outMap.containsKey(src.getValue())) outMap.put(src.getValue(), new ArrayList<>());
			outMap.get(src.getValue()).add(src.getKey());
		}
	}

	private String prefix() {
		return PRESENTABLE_MESSAGE + "[" + conf.getModule() + " - " + conf.model().outDsl() + "]";
	}
}
