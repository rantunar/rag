package com.gen.ai.rag.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class LoadEmbedding {

    @NonNull private final EmbeddingModel embeddingModel;
    @NonNull private final DbConfig dbConfig;

    private static final String resourcePath_policy = "documents/policy";
    private static final String resourcePath_jd = "documents/jd";

    private List<Document> loadPolicy() {
        // Load all documents from a directory and its subdirectories
        // And parsed the document using ApacheTika library
        return ClassPathDocumentLoader.loadDocumentsRecursively(resourcePath_policy, new ApacheTikaDocumentParser());
    }

    private List<Document> loadJd() {
        // Load all documents from a directory and its subdirectories
        // And parsed the document using ApacheTika library
        return ClassPathDocumentLoader.loadDocumentsRecursively(resourcePath_jd, new ApacheTikaDocumentParser());
    }

    @Bean(name = "textSegments")
    public List<TextSegment> getChunks() {
        // Now, we need to split this document into smaller segments, also known as "chunks."
        // This approach allows us to send only relevant segments to the LLM in response to a user query,
        // rather than the entire document. For instance, if a user asks about leave policies,
        // we will identify and send only those segments related to leave.
        // A good starting point is to use a recursive document splitter that initially attempts
        // to split by paragraphs. If a paragraph is too large to fit into a single segment,
        // the splitter will recursively divide it by newlines, then by sentences, and finally by words,
        // if necessary, to ensure each piece of text fits into a single segment.
        List<Document> documents = loadPolicy();
        DocumentSplitter documentSplitter = DocumentSplitters.recursive(300, 20);
        return documentSplitter.splitAll(documents);
    }

    @Bean(name = "embedChunks")
    public List<Embedding> embedChunks(@Qualifier("textSegments") List<TextSegment> textSegments) {
        return embeddingModel.embedAll(textSegments).content();
    }

    @Bean(name = "embeddingStore")
    public EmbeddingStore<TextSegment> storeEmbeddings(@Qualifier("embedChunks") List<Embedding> embeddings,
                                                       @Qualifier("textSegments") List<TextSegment> textSegments) {
        EmbeddingStore<TextSegment> embeddingStore = getTextSegmentEmbeddingStore("policy");
        // remove all existing stored embedding
        embeddingStore.removeAll();
        embeddingStore.addAll(embeddings, textSegments);
        return embeddingStore;
    }

    private EmbeddingStore<TextSegment> getTextSegmentEmbeddingStore(String tableName) {
        return PgVectorEmbeddingStore.builder()
                .host(dbConfig.getHost())
                .port(dbConfig.getPort())
                .database(dbConfig.getDatabase())
                .user(dbConfig.getUsername())
                .password(dbConfig.getPassword())
                .table(tableName)
                .dimension(embeddingModel.dimension())
                .build();
    }

    @Bean(name = "hmpContentRetriever")
    public ContentRetriever getHmpContentRetriever(@Qualifier("embeddingStore") EmbeddingStore<TextSegment> embeddingStore) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.6)
                .filter(MetadataFilterBuilder.metadataKey(Document.FILE_NAME).containsString("HMP"))
                .build();
    }

    @Bean(name = "jdContentRetriever")
    public ContentRetriever getJdContentRetriever() {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(getTextSegmentEmbeddingStore("jd"))
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.6)
                .filter(MetadataFilterBuilder.metadataKey(Document.FILE_NAME).containsString("JD"))
                .build();
    }

    @Bean
    public EmbeddingStoreIngestor getEmbeddingStoreIngestor() {
        var embeddingStore = getTextSegmentEmbeddingStore("jd");
        embeddingStore.removeAll();
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 20))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(loadJd());
        return ingestor;
    }
}
