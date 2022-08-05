package io.intino.master.builder.operations.codegeneration;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class EntityTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("entity"))).output(literal("\npublic abstract class Entity {\n\n\tprotected final String id;\n\tprotected final IMap<String, String> attributes;\n\n\tprotected Entity(TriplesRecord record) {\n\t\tthis.record = requireNonNull(record);\n\t}\n\n\tpublic String id() {\n\t\treturn record.id();\n\t}\n\n\t@Override\n\tpublic boolean equals(Object o) {\n\t\tif (this == o) return true;\n\t\tif (o == null || getClass() != o.getClass()) return false;\n\t\tEntity entity = (Entity) o;\n\t\treturn Objects.equals(id(), entity.id());\n\t}\n\n\t@Override\n\tpublic int hashCode() {\n\t\treturn Objects.hash(id());\n\t}\n\n\t@Override\n\tpublic String toString() {\n\t\treturn id();\n\t}\n}\n\n"))
		);
	}
}