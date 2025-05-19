package hoangnguyen.dev.personal_hub_backend.job;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import hoangnguyen.dev.personal_hub_backend.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageCleanJob {
    private final Cloudinary cloudinary;
    private final RedisTemplate<String, String> redisTemplate;
    private final ImageRepository imageRepository;

    /**
    *   If the image is not used in any post, image will be name is orphaned image
    *   And if image has name is orphaned image, it will be deleted in cloudinary.com
     */

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    public void cleanUpOrphanedImages(){
        Set<String> keys = redisTemplate.keys("temp_image:*");
        if(!keys.isEmpty()) {
            for(String key : keys) {
                String imageUrl = (String) redisTemplate.opsForHash().get(key, "url");
                String publicId = (String) redisTemplate.opsForHash().get(key, "public_id");
//                log.info("imageUrl = {}", imageUrl);
//                log.info("public_id = {}", publicId);
                if(imageUrl != null && publicId != null) {
                    if(!imageRepository.existsByImageUrl(imageUrl)){
                        try{
                            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                            redisTemplate.delete(key);
                            log.info("Deleted orphaned image: {}" , publicId);
                        } catch (Exception e){
                            log.info("Fail to delete orphaned image: {}" , publicId, e);
                        }

                    }
                    else{
                        redisTemplate.delete(key);
                    }
                }
            }
        }
    }
}

