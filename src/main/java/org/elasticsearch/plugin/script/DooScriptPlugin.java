package org.elasticsearch.plugin.script;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.elasticsearch.index.similarity.ScriptedSimilarity;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.script.DocReader;
import org.elasticsearch.script.ScoreScript;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;
import org.elasticsearch.search.lookup.SearchLookup;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Set;

public class DooScriptPlugin extends Plugin implements ScriptPlugin {


    private static class MyExpertScriptEngine implements ScriptEngine {
        @Override
        public String getType() {
            return "expert_scripts";
        }

        @Override
        public <T> T compile(String scriptName, String scriptSource,
                             ScriptContext<T> context, Map<String, String> params) {
            if (context.equals(ScoreScript.CONTEXT) == false) {
                throw new IllegalArgumentException(getType()
                        + " scripts cannot be used for context ["
                        + context.name + "]");
            }
            // we use the script "source" as the script identifier
            if ("pure_df".equals(scriptSource)) {
                ScoreScript.Factory factory = PureDfLeafFactory::new;
                return context.factoryClazz.cast(factory);
            }
            throw new IllegalArgumentException("Unknown script name "
                    + scriptSource);
        }

        @Override
        public void close() {
            // optionally close resources
        }

        @Override
        public Set<ScriptContext<?>> getSupportedContexts() {
            return null;
        }

        private static class PureDfLeafFactory implements ScoreScript.LeafFactory {


            private final Map<String, Object> params;
            private final SearchLookup lookup;
            private final String field;
            private final String term;

            private PureDfLeafFactory(
                    Map<String, Object> params, SearchLookup lookup) {
                if (params.containsKey("field") == false) {
                    throw new IllegalArgumentException(
                            "Missing parameter [field]");
                }
                if (params.containsKey("term") == false) {
                    throw new IllegalArgumentException(
                            "Missing parameter [term]");
                }
                this.params = params;
                this.lookup = lookup;
                field = params.get("field").toString();
                term = params.get("term").toString();
            }

            @Override
            public boolean needs_score() {
                return false;  // Return true if the script needs the score
            }

            @Override
            public ScoreScript newInstance(DocReader docReader) throws IOException {


                return null;
            }

//            @Override
//            public ScoreScript newInstance(LeafReaderContext context)
//                    throws IOException {
//                PostingsEnum postings = context.reader().postings(
//                        new ScriptedSimilarity.Term(field, term));
//                if (postings == null) {
//                    /*
//                     * the field and/or term don't exist in this segment,
//                     * so always return 0
//                     */
//                    return new ScoreScript(params, lookup, context) {
//                        @Override
//                        public double execute() {
//                            return 0.0d;
//                        }
//                    };
//                }
//                return new ScoreScript(params, lookup, context) {
//                    int currentDocid = -1;
//                    @Override
//                    public void setDocument(int docid) {
//                        /*
//                         * advance has undefined behavior calling with
//                         * a docid <= its current docid
//                         */
//                        if (postings.docID() < docid) {
//                            try {
//                                postings.advance(docid);
//                            } catch (IOException e) {
//                                throw new UncheckedIOException(e);
//                            }
//                        }
//                        currentDocid = docid;
//                    }
//                    @Override
//                    public double execute() {
//                        if (postings.docID() != currentDocid) {
//                            /*
//                             * advance moved past the current doc, so this doc
//                             * has no occurrences of the term
//                             */
//                            return 0.0d;
//                        }
//                        try {
//                            return postings.freq();
//                        } catch (IOException e) {
//                            throw new UncheckedIOException(e);
//                        }
//                    }
//                };
//            }
        }
    }

}
