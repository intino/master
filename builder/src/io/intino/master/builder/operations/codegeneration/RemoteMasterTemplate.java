package io.intino.master.builder.operations.codegeneration;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class RemoteMasterTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((allTypes("master","interface"))).output(literal("package ")).output(mark("package")).output(literal(";\n\nimport java.util.List;\nimport ")).output(mark("package")).output(literal(".entities.*;\n\npublic interface Master {\n\t")).output(mark("entity", "getterSignature").multiple("\n\n")).output(literal("\n}")),
			rule().condition((type("master"))).output(literal("package ")).output(mark("package")).output(literal(";\n\nimport com.hazelcast.client.HazelcastClient;\nimport com.hazelcast.client.config.ClientConfig;\nimport com.hazelcast.core.EntryEvent;\nimport com.hazelcast.core.HazelcastInstance;\nimport com.hazelcast.map.IMap;\nimport com.hazelcast.map.listener.EntryAddedListener;\nimport com.hazelcast.map.listener.EntryEvictedListener;\nimport com.hazelcast.map.listener.EntryRemovedListener;\nimport com.hazelcast.map.listener.EntryUpdatedListener;\nimport io.intino.master.model.Triple;\n\nimport ")).output(mark("package")).output(literal(".entities.*;\n\nimport java.util.ArrayList;\nimport java.util.HashMap;\nimport java.util.List;\nimport java.util.Map;\nimport java.util.function.BiConsumer;\nimport java.util.function.Consumer;\n\nimport static io.intino.master.model.Triple.SEPARATOR;\n\npublic class RemoteMaster implements Master {\n\tprivate static BiConsumer<String, Triple> publisher;\n\tprivate final Map<String, Consumer<Triple>> removers = new HashMap<>() {{\n\t\t")).output(mark("entity", "remover").multiple("\n")).output(literal("\n\t}};\n\tprivate final Map<String, Consumer<Triple>> adders = new HashMap<>() {{\n\t\t")).output(mark("entity", "adder").multiple("\n")).output(literal("\n\t}};\n\t")).output(mark("entity", "map").multiple("\n")).output(literal("\n\tprivate final IMap<String, String> hex2subjects;\n\tprivate final IMap<String, String> hex2predicates;\n\n\n\tpublic RemoteMaster() {\n\t\tthis(new ClientConfig());\n\t}\n\n\tpublic RemoteMaster(ClientConfig config) {\n\t\tHazelcastInstance hz = HazelcastClient.newHazelcastClient(config);\n\t\thex2subjects = hz.getMap(\"hex2subjects\");\n\t\thex2predicates = hz.getMap(\"hex2predicates\");\n\t\tIMap<String, String> master = hz.getMap(\"master\");\n\t\tmaster.forEach((key, value) -> {\n\t\t\tfinal String[] subjectVerb = key.split(SEPARATOR);\n\t\t\tadd(new Triple(hex2subjects.get(subjectVerb[0]).toString(), hex2predicates.get(subjectVerb[1]), value));\n\t\t});\n\t\tmaster.addEntryListener(new TripleEntryDispatcher(), true);\n\t\tpublisher = (publisher, triple) -> hz.getTopic(\"request\").publish(publisher + \":\" + triple.toString());\n\t}\n\n\t")).output(mark("entity", "getter").multiple("\n")).output(literal("\n\n\tpublic void publish(String publisher, Triple triple) {\n\t\tif (this.publisher != null) this.publisher.accept(publisher, triple);\n\t}\n\n\tprivate void add(Triple triple) {\n\t\tadders.getOrDefault(typeOf(triple), t -> {}).accept(triple);\n\t}\n\n\tprivate void remove(Triple triple) {\n\t\tremovers.getOrDefault(typeOf(triple), t -> {}).accept(triple);\n\t}\n\n\t")).output(mark("entity", "add").multiple("\n\n")).output(literal("\n\n\t")).output(mark("entity", "remove").multiple("\n\n")).output(literal("\n\n\tprivate String typeOf(Triple triple) {\n\t\treturn triple.subject().split(\":\")[1];\n\t}\n\n\tpublic class TripleEntryDispatcher implements EntryAddedListener<String, String>, EntryUpdatedListener<String, String>, EntryRemovedListener<String, String>, EntryEvictedListener<String, String> {\n\n\t\t@Override\n\t\tpublic void entryAdded(EntryEvent<String, String> event) {\n\t\t\tadd(triple(event));\n\t\t}\n\n\t\t@Override\n\t\tpublic void entryUpdated(EntryEvent<String, String> event) {\n\t\t\tadd(triple(event));\n\t\t}\n\n\t\t@Override\n\t\tpublic void entryRemoved(EntryEvent<String, String> event) {\n\t\t\tremove(triple(event));\n\t\t}\n\n\t\t@Override\n\t\tpublic void entryEvicted(EntryEvent<String, String> event) {\n\t\t\tremove(triple(event));\n\t\t}\n\n\t\tprivate Triple triple(EntryEvent<String, String> event) {\n\t\t\tfinal String[] subjectVerb = event.getKey().split(SEPARATOR);\n\t\t\treturn new Triple(hex2subjects.get(subjectVerb[0]).toString(), hex2predicates.get(subjectVerb[1]), event.getValue());\n\t\t}\n\t}\n}")),
			rule().condition((trigger("remover"))).output(literal("put(\"")).output(mark("name")).output(literal("\", RemoteMaster.this::removeFrom")).output(mark("name", "FirstUpperCase")).output(literal(");")),
			rule().condition((trigger("adder"))).output(literal("put(\"")).output(mark("name")).output(literal("\", RemoteMaster.this::addTo")).output(mark("name", "FirstUpperCase")).output(literal(");")),
			rule().condition((trigger("map"))).output(literal("private final Map<String, ")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "FirstLowerCase")).output(literal("Map = new HashMap<>();")),
			rule().condition((trigger("add"))).output(literal("private void addTo")).output(mark("name", "FirstUpperCase")).output(literal("(Triple triple) {\n\tif (!")).output(mark("name", "firstLowerCase")).output(literal("Map.containsKey(triple.subject())) ")).output(mark("name", "firstLowerCase")).output(literal("Map.put(triple.subject(), new ")).output(mark("name", "FirstUpperCase")).output(literal("(triple.subject(), this));\n\t")).output(mark("name", "firstLowerCase")).output(literal("Map.get(triple.subject()).add(triple);\n}")),
			rule().condition((trigger("remove"))).output(literal("private void removeFrom")).output(mark("name", "FirstUpperCase")).output(literal("(Triple triple) {\n\t")).output(mark("name", "firstLowerCase")).output(literal("Map.computeIfPresent(triple.subject(), (k, v) -> v.remove(triple));\n}")),
			rule().condition((trigger("getter"))).output(literal("public ")).output(mark("name", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(String id) {\n\treturn ")).output(mark("name", "firstLowerCase")).output(literal("Map.get(id);\n}\n\npublic List<")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "Plural", "firstLowerCase")).output(literal("() {\n\treturn new ArrayList<>(")).output(mark("name", "firstLowerCase")).output(literal("Map.values());\n}")),
			rule().condition((trigger("gettersignature"))).output(literal("public ")).output(mark("name", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(String id);\n\npublic List<")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "Plural", "firstLowerCase")).output(literal("();"))
		);
	}
}