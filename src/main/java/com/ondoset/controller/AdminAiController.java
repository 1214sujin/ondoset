package com.ondoset.controller;

import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import com.ondoset.dto.admin.ai.*;
import com.ondoset.service.AdminAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/ai")
public class AdminAiController {

    private final AdminAiService adminAiService;

    @GetMapping("model_version")
    public ResponseEntity<ResponseMessage<GetAdaptModelDTO>> getAdaptModel() {
        return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, adminAiService.getAdaptModel()));
    }

    @GetMapping("list")
    public ResponseEntity<ResponseMessage<GetModelListDTO>> getModelList() {
        return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, adminAiService.getModelList()));
    }

    @PostMapping("select")
    public ResponseEntity<ResponseMessage<AdaptResponseDTO>> setAdaptModel(@RequestBody SelectModelDTO selectModelDTO) {
        return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, adminAiService.setAdaptModel(selectModelDTO)));
    }

    @GetMapping("cfModel")
    public ResponseEntity<ResponseMessage<GetCFModelMetricsDTO>> getCFModelMetrics() {
        return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, adminAiService.getCFModelMetrics()));
    }

    @GetMapping("train")
    public ResponseEntity<ResponseMessage<GetTrainResultDTO>> getTrainResult() {
        return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, adminAiService.getTrainResult()));
    }

    @PostMapping("train")
    public ResponseEntity<ResponseMessage<TrainModelRespDTO>> trainModel(@RequestBody TrainModelReqDTO trainModelReqDTO) {
        return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, adminAiService.trainModel(trainModelReqDTO)));
    }

    @PostMapping("test")
    public ResponseEntity<ResponseMessage<String>> testModel(@RequestBody TestModelReqDTO testModelReqDTO) {
        adminAiService.testModel(testModelReqDTO);
        return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "테스트 성공"));
    }

}
