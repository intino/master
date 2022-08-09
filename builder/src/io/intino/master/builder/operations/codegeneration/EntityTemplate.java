package io.intino.master.builder.operations.codegeneration;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class EntityTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((allTypes("entity","decorable"))).output(literal("package ")).output(mark("package")).output(literal(";\n\npublic class ")).output(mark("name", "FirstUpperCase")).output(literal(" extends Abstract")).output(mark("name", "FirstUpperCase")).output(literal(" {\n\n}")),
			rule().condition((allTypes("entity","class"))).output(literal("package ")).output(mark("package")).output(literal(";\n\nimport io.intino.master.model.Triple;\n\nimport java.util.List;\nimport java.util.HashMap;\nimport java.util.Map;\nimport java.util.function.BiConsumer;\n\npublic class ")).output(mark("abstract")).output(mark("name", "FirstUpperCase")).output(literal(" extends io.intino.master.model.Entity {\n\tprivate static final Map<String, BiConsumer<")).output(mark("name", "FirstUpperCase")).output(literal(", Triple>> setters = new HashMap<>() {{\n\t\t")).output(expression().output(mark("attribute", "set").multiple("\n"))).output(literal("\n\t}};\n\tprivate Master master;\n\t")).output(expression().output(mark("attribute", "wordDeclaration").multiple("\n"))).output(literal("\n\n\t")).output(expression().output(mark("attribute", "field").multiple("\n"))).output(literal("\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal("(String id, Master master) {\n\t\tsuper(id);\n\t\tthis.master = master;\n\t}\n\n\t")).output(mark("attribute", "getter").multiple("\n\n")).output(literal("\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal(" add(Triple triple) {\n\t\tsetters.getOrDefault(triple.predicate(), (")).output(mark("name", "firstLowerCase")).output(literal(", t) -> ")).output(mark("name", "firstLowerCase")).output(literal(".attributes.put(t.predicate(), t.value()))\n\t\t\t\t.accept(this, triple);\n\t\treturn this;\n\t}\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal(" remove(Triple triple) {\n\t\tsetters.getOrDefault(triple.predicate(), (")).output(mark("name", "firstLowerCase")).output(literal(", t) -> ")).output(mark("name", "firstLowerCase")).output(literal(".attributes.put(t.predicate(), null))\n\t\t\t\t.accept(this, null);\n\t\treturn this;\n\t}\n}")),
			rule().condition((type("boolean")), (trigger("set"))).output(literal("put(\"")).output(mark("name")).output(literal("\", (")).output(mark("owner", "firstLowerCase")).output(literal(", t) -> ")).output(mark("owner", "firstLowerCase")).output(literal(".")).output(mark("name", "firstLowerCase")).output(literal(" = t == null ? null : Boolean.parseBoolean(t.value()));")),
			rule().condition((type("integer")), (trigger("set"))).output(literal("put(\"")).output(mark("name")).output(literal("\", (")).output(mark("owner", "firstLowerCase")).output(literal(", t) -> ")).output(mark("owner", "firstLowerCase")).output(literal(".")).output(mark("name", "firstLowerCase")).output(literal(" = t == null ? null : Integer.parseInt(t.value()));")),
			rule().condition((type("real")), (trigger("set"))).output(literal("put(\"")).output(mark("name")).output(literal("\", (")).output(mark("owner", "firstLowerCase")).output(literal(", t) -> ")).output(mark("owner", "firstLowerCase")).output(literal(".")).output(mark("name", "firstLowerCase")).output(literal(" = t == null ? null : Double.parseDouble(t.value()));")),
			rule().condition((type("long")), (trigger("set"))).output(literal("put(\"")).output(mark("name")).output(literal("\", (")).output(mark("owner", "firstLowerCase")).output(literal(", t) -> ")).output(mark("owner", "firstLowerCase")).output(literal(".")).output(mark("name", "firstLowerCase")).output(literal(" = t == null ? null : Long.parseLong(t.value()));")),
			rule().condition((type("word")), (trigger("set"))).output(literal("put(\"")).output(mark("name")).output(literal("\", (")).output(mark("owner", "firstLowerCase")).output(literal(", t) -> ")).output(mark("owner", "firstLowerCase")).output(literal(".")).output(mark("name", "firstLowerCase")).output(literal(" = t == null ? null : ")).output(mark("type", "firstUpperCase")).output(literal(".valueOf(t.value()));")),
			rule().condition((type("string")), (trigger("set"))).output(literal("put(\"")).output(mark("name")).output(literal("\", (")).output(mark("owner", "firstLowerCase")).output(literal(", t) -> ")).output(mark("owner", "firstLowerCase")).output(literal(".")).output(mark("name", "firstLowerCase")).output(literal(" = t == null ? null : t.value());")),
			rule().condition((type("word")), (trigger("worddeclaration"))).output(literal("public enum ")).output(mark("name", "firstUpperCase")).output(literal(" {\n\t")).output(mark("value").multiple(", ")).output(literal(";\n}")),
			rule().condition(not(type("entity")), (trigger("field"))).output(literal("private ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(" ")).output(expression().output(literal("= ")).output(mark("defaultValue"))).output(literal(";")),
			rule().condition((type("entity")), (trigger("getter"))).output(literal("public ")).output(mark("name", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn master.")).output(mark("name", "firstLowerCase")).output(literal("(attributes.get(\"")).output(mark("name", "firstLowerCase")).output(literal("\"));\n}")),
			rule().condition((trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn ")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((trigger("abstract"))).output(literal("Abstract"))
		);
	}
}