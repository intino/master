package io.intino.master.builder.operations.codegeneration;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class StructTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("struct"))).output(literal("package ")).output(mark("package")).output(literal(".structs;\n\npublic class ")).output(mark("name", "FirstUpperCase")).output(literal(" {\n\t")).output(mark("attribute", "field").multiple("\n")).output(literal("\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal("(")).output(mark("attribute", "parameter").multiple(", ")).output(literal(") {\n\t\t")).output(mark("attribute", "assign").multiple("\n")).output(literal("\n\t}\n\n\t")).output(mark("attribute", "getter").multiple("\n\n")).output(literal("\n}")),
			rule().condition((trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn ")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((trigger("assign"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((trigger("field"))).output(literal("private final ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((trigger("parameter"))).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase"))
		);
	}
}