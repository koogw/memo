package com.velokan.memo.memo.Item;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.sql.SQLOutput;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/home")
    String memoItem(Model model) {
        List<Item> items = itemService.findAll();
        model.addAttribute("items", items);
        return "Home.html";
    }

    @GetMapping("/addmemo")
    String addMemo(Model model) {
        List<Item> items = itemService.findAll();
        model.addAttribute("items", items);
        return "addmemo.html";
    }

    @PostMapping("/realadd")
    String realadd(@ModelAttribute Item item) {
        itemService.save(item);
        return "redirect:/detailMemo/" + item.getId();
    }

    @PostMapping("/addmemo")
    String addMemo(ItemDto itemDto) {
        log.info("item dto = {}", itemDto);

        Item item = Item.builder()
                .title(itemDto.getTitle())
                .content(itemDto.getContent())
                .id(itemDto.getId())
                .release_date(LocalDate.parse(itemDto.getRelease_date()))
                .videourl(itemDto.getVideourl())
                .audiourl(itemDto.getAudiourl())
                .build();
        itemService.save(item);
        return "redirect:/detailMemo/" + item.getId();
    }

    @GetMapping("/detailMemo/{id}")
    String detailMemo(@PathVariable Long id, Model model) {
        // ID에 해당하는 Item 객체를 가져옵니다.
        Item item = itemService.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found")); // 예외 처리

        model.addAttribute("item", item); // 단일 아이템 추가
        List<Item> items = itemService.findAll(); // 모든 아이템을 가져옴
        model.addAttribute("items", items); // 모든 아이템을 모델에 추가

        return "detailMemo";
    }

    @PostMapping("/detailMemo/{id}")
    String updateMemo(@PathVariable Long id,
                      @RequestParam String title,
                      @RequestParam String editordata,
                      @RequestParam String fileName,
                      @RequestParam String fileType,
                      @RequestParam String videourl,
                      @RequestParam String audiourl) {
        Item item = itemService.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found")); // 예외 처리

        item.setTitle(title);
        item.setContent(editordata);
        item.setVideourl(videourl);
        item.setAudiourl(audiourl);
        itemService.updateItem(id, item);

        return "redirect:/detailMemo/" + id; // 수정된 아이템의 상세 페이지로 리다이렉트
    }

    @PostMapping("/deleteItem/{id}")
    public String deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return "redirect:/home";
    }

    private static String UPLOAD_DIR = "C:\\memo\\src\\main\\resources\\static\\img"; // 실제 경로로 수정
    public static final String VIDEO_UPLOAD_DIR = "C:\\memo\\src\\main\\resources\\static\\videos"; // 비디오 파일 저장 경로
    public static final String AUDIO_UPLOAD_DIR = "C:\\memo\\src\\main\\resources\\static\\audios"; // 오디오 파일 저장 경로

    @PostMapping("/uploadSummernoteImageFile")
    @ResponseBody
    public ResponseEntity<?> uploadSummernoteImageFile(@RequestParam("file") MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + File.separator + originalFileName);
        Path path1 = Paths.get(originalFileName);

        try {
            // 파일이 존재하는 경우, 새로운 이름 생성
            int count = 1;
            while (Files.exists(path)) {
                // 파일 이름에 카운터 추가
                String newFileName = originalFileName.substring(0, originalFileName.lastIndexOf('.'))
                        + "_" + count++
                        + originalFileName.substring(originalFileName.lastIndexOf('.'));
                path = Paths.get(UPLOAD_DIR + File.separator + newFileName);
            }

            // 파일 저장
            Files.copy(file.getInputStream(), path);

            // 클라이언트가 접근할 수 있는 URL
            String fileUrl = "http://localhost:8080/img/" + path1;
            return ResponseEntity.ok(Map.of("url", fileUrl));
        } catch (IOException e) {
            e.printStackTrace(); // 예외 메시지를 콘솔에 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패: " + e.getMessage());
        }
    }


    @PostMapping("/deleteMedia")
    @ResponseBody
    public ResponseEntity<?> deleteMedia(@RequestParam String src) {
        // src에서 파일 경로를 추출하여 삭제하는 로직 구현
        // 예: src = "/uploaded/myImage.jpg"
        String filePath = UPLOAD_DIR + src; // 실제 파일 경로
        File file = new File(filePath);
        if (file.delete()) {
            return ResponseEntity.ok("파일 삭제 성공");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 삭제 실패");
        }
    }
    @GetMapping("/videos/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> serveVideo(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(VIDEO_UPLOAD_DIR).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                        .body(resource);
            } else {
                throw new RuntimeException("파일을 찾을 수 없습니다.");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("파일 경로가 잘못되었습니다.");
        }
    }

    @PostMapping("/uploadSummernoteVideoFile")
    @ResponseBody
    public ResponseEntity<?> uploadSummernoteVideoFile(@RequestParam("file") MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String fileName = UUID.randomUUID() + "_" + originalFileName;  // 파일 이름에 UUID 추가하여 중복 방지
        Path path = Paths.get(VIDEO_UPLOAD_DIR, fileName);
        Path path1 = Paths.get(fileName);

        try {
            // 비디오 파일 저장
            Files.copy(file.getInputStream(), path);

            // 저장된 비디오 파일의 URL 반환
            String fileUrl = "http://localhost:8080/videos/" + fileName;
            return ResponseEntity.ok(Map.of("url", fileUrl));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("비디오 업로드 실패: " + e.getMessage());
        }
    }

    @GetMapping("/audios/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> serveAudio(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(AUDIO_UPLOAD_DIR).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "audio/wav")
                        .body(resource);
            } else {
                throw new RuntimeException("파일을 찾을 수 없습니다.");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("파일 경로가 잘못되었습니다.");
        }
    }

    // 오디오 파일 업로드를 처리하는 메서드
    @PostMapping("/uploadSummernoteAudioFile")
    @ResponseBody
    public ResponseEntity<?> uploadSummernoteAudioFile(@RequestParam("file") MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String fileName = UUID.randomUUID() + "_" + originalFileName;  // 파일 이름에 UUID 추가하여 중복 방지
        Path path = Paths.get(AUDIO_UPLOAD_DIR, fileName);
        Path path1 = Paths.get(fileName);
        try {
            // 오디오 파일 저장
            Files.copy(file.getInputStream(), path);

            // 저장된 오디오 파일의 URL 반환
            String fileUrl = "http://localhost:8080/audios/" + path1;
            return ResponseEntity.ok(Map.of("url", fileUrl));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("오디오 업로드 실패: " + e.getMessage());
        }
    }

}
