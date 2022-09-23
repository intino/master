package io.intino.master.builder.operations.codegeneration;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class EntityTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((allTypes("entity","decorable"))).output(literal("package ")).output(mark("package")).output(literal(".entities;\n\npublic class ")).output(mark("name", "FirstUpperCase")).output(literal(" extends Abstract")).output(mark("name", "FirstUpperCase")).output(literal(" {\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal("(String id, ")).output(mark("package")).output(literal(".MasterClient master) {\n\t\tsuper(id, master);\n\t}\n}")),
			rule().condition((allTypes("entity","class"))).output(literal("package ")).output(mark("package")).output(literal(".entities;\n\nimport io.intino.master.model.Triple;\n\nimport java.util.List;\nimport java.util.HashMap;\nimport java.util.Map;\nimport java.util.function.BiConsumer;\n\npublic")).output(expression().output(literal(" ")).output(mark("isAbstract", "firstLowerCase"))).output(literal(" class ")).output(mark("abstract")).output(mark("name", "FirstUpperCase")).output(literal(" extends ")).output(mark("parent")).output(literal(" {\n\n\tprivate ")).output(mark("package")).output(literal(".MasterClient master;\n\n\t")).output(expression().output(mark("attribute", "wordDeclaration").multiple("\n"))).output(literal("\n\t")).output(expression().output(mark("attribute", "field").multiple("\n"))).output(literal("\n\n\tpublic ")).output(mark("abstract")).output(mark("name", "FirstUpperCase")).output(literal("(String id, ")).output(mark("package")).output(literal(".MasterClient master) {\n\t\t")).output(mark("parent", "super")).output(literal("\n\t\tthis.master = master;\n\t}\n\n\t")).output(expression().output(mark("attribute", "getter").multiple("\n\n"))).output(literal("\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal(" add(Triple triple) {\n\t\tswitch(triple.predicate()) {\n\t\t\t")).output(expression().output(mark("attribute", "set").multiple("\n"))).output(literal("\n\t\t\tdefault:\n\t\t\t super.add(triple);\n\t\t\t break;\n\t\t}\n\t\treturn (")).output(mark("name", "FirstUpperCase")).output(literal(") this;\n\t}\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal(" remove(Triple triple) {\n\t\tswitch(triple.predicate()) {\n\t\t\t")).output(expression().output(mark("attribute", "set").multiple("\n"))).output(literal("\n\t\t\tdefault:\n\t\t\tsuper.remove(triple);\n\t\t\tbreak;\n\t\t}\n\t\treturn (")).output(mark("name", "FirstUpperCase")).output(literal(") this;\n\t}\n\n\t")).output(expression().output(literal("public List<Triple> asTriples() {\r")).output(literal("\n")).output(literal("\tfinal java.util.ArrayList<Triple> triples = new java.util.ArrayList<>();\r")).output(literal("\n")).output(literal("\t")).output(mark("attribute", "asTriple").multiple("\n")).output(literal("\n")).output(literal("\tsuper.extraAttributes().entrySet().stream().map(e -> new Triple(id().get(), e.getKey(), e.getValue())).forEach(triples::add);\r")).output(literal("\n")).output(literal("\treturn triples;\r")).output(literal("\n")).output(literal("}"))).output(literal("\n}")),
			rule().condition((attribute("", "io.intino.master.model.Entity")), (trigger("super"))).output(literal("super(id);")),
			rule().condition((trigger("super"))).output(literal("super(id, master);")),
			rule().condition((type("boolean")), (trigger("set"))).output(literal("case \"")).output(mark("name")).output(literal("\":\n \tthis.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Boolean.parseBoolean(triple.value());\n \tbreak;")),
			rule().condition((type("integer")), (trigger("set"))).output(literal("case \"")).output(mark("name")).output(literal("\":\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Integer.parseInt(triple.value());\n\tbreak;")),
			rule().condition((type("real")), (trigger("set"))).output(literal("case \"")).output(mark("name")).output(literal("\":\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Double.parseDouble(triple.value());\n\tbreak;")),
			rule().condition((type("long")), (trigger("set"))).output(literal("case \"")).output(mark("name")).output(literal("\":\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Long.parseLong(triple.value());\n\tbreak;")),
			rule().condition((type("word")), (trigger("set"))).output(literal("case \"")).output(mark("name")).output(literal("\":\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : ")).output(mark("type", "firstUpperCase")).output(literal(".valueOf(triple.value());\n\tbreak;")),
			rule().condition((type("string")), (trigger("set"))).output(literal("case \"")).output(mark("name")).output(literal("\":\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value();\n\tbreak;")),
			rule().condition((type("entity")), (trigger("set"))).output(literal("case \"")).output(mark("name")).output(literal("\":\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal("Reference = triple.value();\n\tbreak;")),
			rule().condition((type("date")), (trigger("set"))).output(literal("case \"")).output(mark("name")).output(literal("\":\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal(" = java.time.LocalDate.parse(triple.value(), java.time.format.DateTimeFormatter.ofPattern(\"")).output(mark("format")).output(literal("\"));\n\tbreak;")),
			rule().condition((type("datetime")), (trigger("set"))).output(literal("case \"")).output(mark("name")).output(literal("\":\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal(" = java.time.LocalDateTime.parse(triple.value(), java.time.format.DateTimeFormatter.ofPattern(\"")).output(mark("format")).output(literal("\"));\n\tbreak;")),
			rule().condition((type("instant")), (trigger("set"))).output(literal("case \"")).output(mark("name")).output(literal("\":\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal(" = java.time.Instant.ofEpochMilli(Long.parseLong(triple.value()));\n\tbreak;")),
			rule().condition((type("map")), (trigger("set"))).output(literal("case \"")).output(mark("name")).output(literal("\":\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : java.util.Arrays.stream(triple.value().split(\";\"))\n\t\t.map(e -> e.split(\"=\"))\n\t\t.collect(java.util.stream.Collectors.toMap(e -> e")).output(expression().output(literal("0"))).output(literal(".trim(), e -> e")).output(expression().output(literal("1"))).output(literal(".trim()));\n\tbreak;")),
			rule().condition((type("struct")), (trigger("set"))).output(literal("case \"")).output(mark("name")).output(literal("\": {\n\tif (triple.value() == null) {\n\t\tthis.")).output(mark("name", "firstLowerCase")).output(literal(" = null;\n\t} else {\n\t\tList<String> values = java.util.Arrays.stream(triple.value().split(\",\", -1)).map(v -> v.trim()).collect(java.util.stream.Collectors.toList());\n\t\tthis.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : ")).output(mark("struct", "call")).output(literal(";\n\t}\n\tbreak;\n}")),
			rule().condition((type("word")), (trigger("astriple"))).output(literal("triples.add(new Triple(id().get(), \"")).output(mark("name", "firstLowerCase")).output(literal("\", ")).output(mark("name", "firstLowerCase")).output(literal(".name()));")),
			rule().condition((type("entity")), (trigger("astriple"))).output(literal("triples.add(new Triple(id().get(), \"")).output(mark("name", "firstLowerCase")).output(literal("\", String.valueOf(")).output(mark("name", "firstLowerCase")).output(literal("Reference)));")),
			rule().condition((anyTypes("date","datetime")), (trigger("astriple"))).output(literal("triples.add(new Triple(id().get(), \"")).output(mark("name", "firstLowerCase")).output(literal("\", ")).output(mark("name", "firstLowerCase")).output(literal(".format(java.time.format.DateTimeFormatter.ofPattern(\"")).output(mark("format")).output(literal("\"))));")),
			rule().condition((type("instant")), (trigger("astriple"))).output(literal("triples.add(new Triple(id().get(), \"")).output(mark("name", "firstLowerCase")).output(literal("\", String.valueOf(")).output(mark("name", "firstLowerCase")).output(literal(".toEpochMilli())));")),
			rule().condition((trigger("astriple"))).output(literal("triples.add(new Triple(id().get(), \"")).output(mark("name", "firstLowerCase")).output(literal("\", String.valueOf(")).output(mark("name", "firstLowerCase")).output(literal(")));")),
			rule().condition((type("word")), (trigger("worddeclaration"))).output(literal("public enum ")).output(mark("name", "firstUpperCase")).output(literal(" {")).output(mark("value").multiple(", ")).output(literal("}")),
			rule().condition((type("struct")), (trigger("field"))).output(literal("protected ")).output(mark("package")).output(literal(".structs.")).output(mark("struct", "structName")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(expression().output(literal(" = ")).output(mark("defaultValue"))).output(literal(";")),
			rule().condition((type("entity")), (trigger("field"))).output(literal("protected String ")).output(mark("name", "firstLowerCase")).output(literal("Reference;")),
			rule().condition((type("date")), (trigger("field"))).output(literal("protected java.time.LocalDate ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((type("datetime")), (trigger("field"))).output(literal("protected java.time.LocalDateTime ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((type("instant")), (trigger("field"))).output(literal("protected java.time.Instant ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition(not(type("entity")), (trigger("field"))).output(literal("protected ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(expression().output(literal(" = ")).output(mark("defaultValue"))).output(literal(";")),
			rule().condition((type("word")), (trigger("defaultvalue"))).output(mark("type")).output(literal(".")).output(mark("value")),
			rule().condition((trigger("defaultvalue"))).output(mark("value")),
			rule().condition((type("struct")), (trigger("getter"))).output(literal("public ")).output(mark("package")).output(literal(".structs.")).output(mark("struct", "structName")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn this.")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((type("entity")), (trigger("getter"))).output(literal("public ")).output(mark("entity", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn master.")).output(mark("entity", "firstLowerCase")).output(literal("(")).output(mark("name", "firstLowerCase")).output(literal("Reference);\n}")),
			rule().condition((type("date")), (trigger("getter"))).output(literal("public java.time.LocalDate ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn this.")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((type("datetime")), (trigger("getter"))).output(literal("public java.time.LocalDateTime ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn this.")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((type("instant")), (trigger("getter"))).output(literal("public java.time.Instant ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn this.")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn ")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((trigger("abstract"))).output(literal("Abstract")),
			rule().condition((trigger("structname"))).output(mark("name", "firstUpperCase")),
			rule().condition((trigger("call"))).output(literal("new ")).output(mark("package")).output(literal(".structs.")).output(mark("name", "firstUpperCase")).output(literal("(")).output(mark("attribute", "parse").multiple(", ")).output(literal(")")),
			rule().condition((type("boolean")), (trigger("parse"))).output(literal("Boolean.parseBoolean(values.get(")).output(mark("index")).output(literal("))")),
			rule().condition((type("int")), (trigger("parse"))).output(literal("Integer.parseInt(values.get(")).output(mark("index")).output(literal("))")),
			rule().condition((type("double")), (trigger("parse"))).output(literal("Double.parseDouble(values.get(")).output(mark("index")).output(literal("))")),
			rule().condition((type("date")), (trigger("parse"))).output(literal("java.time.LocalDate.parse(values.get(")).output(mark("index")).output(literal("));")),
			rule().condition((type("datetime")), (trigger("parse"))).output(literal("java.time.LocalDateTime.parse(values.get(")).output(mark("index")).output(literal("));")),
			rule().condition((type("instant")), (trigger("parse"))).output(literal("java.time.Instant.parse(values.get(")).output(mark("index")).output(literal("));")),
			rule().condition((trigger("parse"))).output(literal("values.get(")).output(mark("index")).output(literal(")"))
		);
	}
}