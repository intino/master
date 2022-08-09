package io.intino.master.builder.operations.codegeneration;


import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.Language;
import io.intino.magritte.builder.core.CompilerConfiguration;
import io.intino.magritte.lang.model.Aspect;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Parameter;
import io.intino.magritte.lang.model.Tag;

import java.util.HashMap;
import java.util.Map;

import static io.intino.magritte.builder.utils.Format.firstUpperCase;
import static io.intino.magritte.builder.utils.Format.javaValidName;

public class EntityFrameCreator {
	private static final String DOT = ".";
	private static final Map<String, String> types = Map.of(
			"String", "String",
			"Double", "double",
			"Integer", "int",
			"Boolean", "boolean",
			"Entity", "io.intino.master.model.Entity",
			"Long", "long"
	);

	private static final Map<String, String> listTypes = Map.of(
			"String", "List<String>",
			"Double", "List<Double>",
			"Integer", "List<Integer>",
			"Boolean", "List<Boolean",
			"Entity", "List<io.intino.master.model.Entity>",
			"Long", "List<Long>"
	);
	private final CompilerConfiguration conf;
	private final Language language;

	public EntityFrameCreator(CompilerConfiguration conf, Language language) {
		this.conf = conf;
		this.language = language;
	}

	public Map<String, Frame> create(Node node) {
		Map<String, Frame> map = new HashMap<>(4);
		FrameBuilder builder = frameOf(node);
		map.put(calculateEntityPath(node, conf.workingPackage()), builder.toFrame());
		if (node.is(Tag.Decorable))
			map.put(calculateDecorableEntityPath(node, conf.workingPackage()), builder.add("decorable").toFrame());
		return map;
	}

	private FrameBuilder frameOf(Node node) {
		FrameBuilder builder = new FrameBuilder("entity", "class")
				.add("package", conf.workingPackage())
				.add("name", node.name())
				.add("attribute", node.components().stream().map(this::attrFrameOf).toArray());
		if (node.is(Tag.Decorable)) builder.add("abstract", "abstract");
		return builder;
	}

	private Frame attrFrameOf(Node c) {
		FrameBuilder builder = new FrameBuilder().add("attribute");
		c.appliedAspects().forEach(aspect -> builder.add(aspect.type()));
		builder.add("name", c.name());
		builder.add("owner", c.container().name());
		builder.add("type", type(c));
		Parameter values = parameter(c, "values");
		if (values != null) builder.add("value", values.values().stream().map(Object::toString).toArray());
		Parameter defaultValue = parameter(c, "defaultValue");
		if (defaultValue != null) builder.add("defaultValue", defaultValue.values().get(0).toString());
		Parameter entity = parameter(c, "entity");
		if (entity != null) builder.add("entity", entity.values().get(0).toString());
		return builder.toFrame();
	}

	private String type(Node node) {
		String aspect = node.appliedAspects().stream().map(Aspect::type).filter(a -> !a.equals("List")).findFirst().orElse("");
		boolean list = node.appliedAspects().stream().anyMatch(a -> a.type().equals("List"));
		if (!list) return types.getOrDefault(aspect, firstUpperCase().format(node.name()).toString());
		return listTypes.getOrDefault(aspect, "List<" + firstUpperCase().format(node.name()).toString() + ">");

	}

	private Parameter parameter(Node c, String name) {
		return c.parameters().stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
	}

	private String calculateEntityPath(Node node, String aPackage) {
		return aPackage + DOT + (node.is(Tag.Decorable) ? "Abstract" : "") + firstUpperCase().format(javaValidName().format(node.name()).toString());
	}

	private String calculateDecorableEntityPath(Node node, String aPackage) {
		return aPackage + DOT + firstUpperCase().format(javaValidName().format(node.name()).toString());
	}
}
