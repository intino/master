package io.intino.master.builder.operations.codegeneration;


import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.builder.core.CompilerConfiguration;
import io.intino.magritte.builder.model.Model;
import io.intino.magritte.builder.utils.Format;
import io.intino.magritte.lang.model.Node;

import java.util.Map;

public class EntityFrameCreator {

	private final CompilerConfiguration conf;
	private final String languageName;
	private final Model model;

	public EntityFrameCreator(CompilerConfiguration conf, String languageName, Model model) {
		this.conf = conf;
		this.languageName = languageName;
		this.model = model;
	}

	public String render(Node entity) {
		final FrameBuilder builder = new FrameBuilder("entity").add("name", entity.name());
		return Format.customize(new EntityTemplate()).render(builder.toFrame());
	}

	public Map.Entry<String, Frame> create(Node node) {
		return null;
	}
}
