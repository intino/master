package io.intino.master.builder.operations.codegeneration;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class EntityTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("entity"))).output(literal("package io.intino.master.accessor;\n\nimport io.intino.master.model.Triple;\n\nimport java.util.HashMap;\nimport java.util.Map;\nimport java.util.function.BiConsumer;\n\npublic class ")).output(mark("name", "FirstUpperCase")).output(literal(" extends io.intino.master.model.Entity {\n\tprivate static final Map<String, BiConsumer<")).output(mark("name", "FirstUpperCase")).output(literal(", Triple>> setters = new HashMap<>() {{\n\t\t")).output(mark("attribute", "set").multiple("\n")).output(literal("\n\t}};\n\n\t")).output(mark("attribute", "field").multiple("\n")).output(literal("\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal("(String id) {\n\t\tsuper(id);\n\t}\n\n\t")).output(mark("attribute", "getter").multiple("\n")).output(literal("\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal(" add(Triple triple) {\n\t\tsetters.getOrDefault(triple.predicate(), (")).output(mark("name", "firstLowerCase")).output(literal(", t) -> ")).output(mark("name", "firstLowerCase")).output(literal(".attributes.put(t.predicate(), t.value()))\n\t\t\t\t.accept(this, triple);\n\t\treturn this;\n\t}\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal(" remove(Triple triple) {\n\t\tsetters.getOrDefault(triple.predicate(), (")).output(mark("name", "firstLowerCase")).output(literal(", t) -> ")).output(mark("name", "firstLowerCase")).output(literal(".attributes.put(t.predicate(), null))\n\t\t\t\t.accept(this, null);\n\t\treturn this;\n\t}\n}")),
			rule().condition((trigger("set"))).output(literal("put(\"")).output(mark("name")).output(literal("\", (")).output(mark("owner", "firstLowerCase")).output(literal(", t) -> ")).output(mark("owner", "firstLowerCase")).output(literal(".")).output(mark("name", "firstLowerCase")).output(literal(" = t == null ? null : t.value());")),
			rule().condition((trigger("getter"))).output(literal("public String ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn ")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((trigger("getter"))).output(literal("private ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(";"))
		);
	}
}