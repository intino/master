package io.intino.master.data.validation.validators;

import io.intino.master.data.validation.Issue;
import io.intino.master.data.validation.TripleSource;
import io.intino.master.data.validation.TripleValidator;
import io.intino.master.model.Triple;

import java.util.stream.Stream;

import static io.intino.master.core.Master.NONE_TYPE;
import static io.intino.master.data.validation.Issue.Type.NO_TYPE;
import static io.intino.master.model.Triple.typeOf;

public class TypeTripleValidator implements TripleValidator {

	@Override
	public Stream<Issue> validate(String tripleLine, TripleSource source) {
		Triple triple = new Triple(tripleLine);
		return hasNoType(triple.subject())
				? Stream.of(Issue.error(NO_TYPE, "Triple (" + triple.subject() + ") subject must have a type").source(source))
				: Stream.empty();
	}

	private boolean hasNoType(String subject) {
		final String type = typeOf(subject);
		return type == null || type.equals(NONE_TYPE);
	}
}
