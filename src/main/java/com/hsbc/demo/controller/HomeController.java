package com.hsbc.demo.controller;

import com.hsbc.demo.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Controller
public class HomeController {

    private static final String BASE_PATH = "/image";
    private static final String FILENAME = "{filename:.+}";
    private static Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final ImageService imageService;

    public HomeController(ImageService imageService){
        this.imageService = imageService;
    }

    @GetMapping(value=BASE_PATH + "/" + FILENAME + "/raw", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public Mono<ResponseEntity<?>> oneRawImage(@PathVariable String filename){
        return imageService.findOneImage(filename)
                .map(resource -> {
                    try{
                        logger.info("The filename = " + filename);
                        logger.info("The resource = " + resource);
                        return ResponseEntity.ok()
                                .contentLength(resource.contentLength())
                                .body(new InputStreamResource(resource.getInputStream()));
                    } catch (IOException e){
                        logger.error(e.toString());
                        return ResponseEntity.badRequest()
                                .body("Couldn't find " + filename + " ==> " + e.getMessage());
                    }
                });
    }

    @PostMapping(value=BASE_PATH)
    public Mono<String> createFile(@RequestPart (name = "file") Flux<FilePart> files){
        return imageService.createImage(files).then(Mono.just("redirect:/"));
    }

    @DeleteMapping(BASE_PATH + "/" + FILENAME)
    public Mono<String> deleteFile(@PathVariable String filename){
        return imageService.deleteImage(filename).then(Mono.just("redirect:/"));
    }

    @GetMapping("/")
    public Mono<String> index(Model model){
        model.addAttribute("images", imageService.findAllImages());
        return Mono.just("index");
    }

}
