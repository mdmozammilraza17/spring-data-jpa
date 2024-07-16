/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.jpa.repository.query;

import static org.springframework.data.jpa.repository.query.QueryTokens.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.util.CollectionUtils;

/**
 * Utility class encapsulating common query transformations.
 *
 * @author Mark Paluch
 * @since 3.2.5
 */
class QueryTransformers {

	static CountSelectionTokenStream filterCountSelection(QueryTokenStream selection) {

		List<QueryToken> target = new ArrayList<>(selection.size());
		boolean skipNext = false;
		boolean skipNextWitespace = false;
		boolean containsNew = false;

		int openParentesis = 0;

		for (QueryToken token : (selection instanceof QueryRenderer qr) ? qr.renderTokens() : selection) {

			if (skipNext) {

				if(!token.equals(TOKEN_SPACE)) {
					skipNext = false;
					skipNextWitespace = true;
				}
				continue;
			}

			if(skipNextWitespace) {
				skipNextWitespace=false;
				if(token.equals(TOKEN_SPACE)) {
					continue;
				}
			}

			if(token.equals(TOKEN_OPEN_PAREN)) {
				openParentesis++;
			} else if (token.equals(TOKEN_CLOSE_PAREN)) {
				openParentesis--;
			}

			if (openParentesis == 0 && token.equals(TOKEN_AS)) {
				skipNext = true;
				continue;
			}

			if (!token.equals(TOKEN_COMMA) && token.isExpression()) {
				token = QueryTokens.token(token.value());
			}

			if (!containsNew && token.value().contains("new")) {
				containsNew = true;
			}

			target.add(token);
		}

		if(!target.isEmpty() && CollectionUtils.lastElement(target).equals(TOKEN_SPACE)) {
			target.remove(target.size()-1);
		}
		return new CountSelectionTokenStream(target, containsNew);
	}

	static class CountSelectionTokenStream implements QueryTokenStream {

		private final List<QueryToken> tokens;
		private final boolean requiresPrimaryAlias;

		public CountSelectionTokenStream(List<QueryToken> tokens, boolean requiresPrimaryAlias) {
			this.tokens = tokens;
			this.requiresPrimaryAlias = requiresPrimaryAlias;
		}

		@Override
		public Iterator<QueryToken> iterator() {
			return tokens.iterator();
		}

		@Override
		public List<QueryToken> toList() {
			return tokens;
		}

		@Override
		public int size() {
			return tokens.size();
		}

		@Override
		public boolean isExpression() {
			return true;
		}

		@Override
		public boolean isEmpty() {
			return tokens.isEmpty();
		}

		public boolean requiresPrimaryAlias() {
			return requiresPrimaryAlias;
		}

	}

}
