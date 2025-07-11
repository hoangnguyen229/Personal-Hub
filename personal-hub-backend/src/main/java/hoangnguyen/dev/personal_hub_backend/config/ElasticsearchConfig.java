//package hoangnguyen.dev.personal_hub_backend.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.elasticsearch.client.ClientConfiguration;
//import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
//import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
//
//@Configuration
//@EnableElasticsearchRepositories(basePackages = "hoangnguyen.dev.personal_hub_backend.repository")
//public class   ElasticsearchConfig extends ElasticsearchConfiguration {
//
//    @Override
//    public ClientConfiguration clientConfiguration() {
//        return ClientConfiguration.builder()
//                .connectedTo("elasticsearch:9200")
//                .withBasicAuth("elastic", "changeme") // Replace with your actual credentials
//                .build();
//    }
//}
