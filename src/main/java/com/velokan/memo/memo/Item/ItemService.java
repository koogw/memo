package com.velokan.memo.memo.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public List<Item> findAll() {
        return itemRepository.findAll();
    }
    public void save(Item item) {
        itemRepository.save(item);
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }
    public void deleteItem(Long id) {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isPresent()) {
            System.out.println("삭제할 아이템: " + item.get());
            itemRepository.deleteById(id);
        } else {
            System.out.println("해당 아이템을 찾을 수 없습니다: " + id);
        }
    }
    public void updateItem(Long id, Item newItem) {
        Optional<Item> existingItem = itemRepository.findById(id);
        if (existingItem.isPresent()) {
            Item itemToUpdate = existingItem.get();
            itemToUpdate.setTitle(newItem.getTitle());
            itemToUpdate.setContent(newItem.getContent());
            itemToUpdate.setAudiourl(newItem.getAudiourl());
            itemToUpdate.setVideourl(newItem.getVideourl());
            // 다른 필드들도 필요시 업데이트
            itemRepository.save(itemToUpdate);
        } else {
            throw new RuntimeException("아이템을 찾을 수 없습니다: " + id);
        }
    }
    public void saveVideoUrl(Long id, String videourl) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setVideourl(videourl); // 비디오 URL 설정
        itemRepository.save(item); // 변경 사항 저장
    }
}
