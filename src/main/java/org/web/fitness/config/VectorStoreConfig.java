package org.web.fitness.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * 本地知识库设置
 */
@Configuration
public class VectorStoreConfig {
    private static final Logger logger = LoggerFactory.getLogger(VectorStoreConfig.class);
    @Value("classpath:knowledge-base/badao-internal.txt")
    private Resource knowResource;

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // 创建内存向量库（开发演示用）
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    public CommandLineRunner loadDocuments(VectorStore vectorStore) {
        return args -> {
            // 1. 使用 Tika 读取文档（自动检测文件类型）
            boolean exists = knowResource.exists();
            //判断本地知识库文件是否存在，存在就将其向量化，不存在就不向量化
            if(exists){
                DocumentReader reader=new TikaDocumentReader(knowResource);
                List<Document> documents = reader.get();
                logger.info("共读取到 {} 个文档", documents.size());
                // 2. 文本分块（TokenTextSplitter 按语义切分，更适合中文）
                TokenTextSplitter splitter=TokenTextSplitter.builder()
                        .withChunkSize(300) // 每个块最多 300 token
                        .withMinChunkSizeChars(50) // 最小块字符数，避免出现极短碎片（替代原来 minChunkSize 的功能）
                        .withMinChunkLengthToEmbed(5) // 保留默认，过滤极短内容
                        .withKeepSeparator(true)  // 保留原文换行等分隔符
                        .build();
                List<Document> apply = splitter.apply(documents);
                logger.info("文本切分为 {} 个片段", apply.size());
                // 3. 写入向量数据库（自动调用 EmbeddingModel 向量化）
                vectorStore.add(apply);
                logger.info("向量化完成，向量库初始化成功！");
            }
        };
    }
}
