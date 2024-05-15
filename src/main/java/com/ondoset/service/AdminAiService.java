package com.ondoset.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ondoset.domain.Metrics;
import com.ondoset.domain.Model;
import com.ondoset.dto.admin.ai.*;
import com.ondoset.repository.MetricsRepository;
import com.ondoset.repository.ModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Service
public class AdminAiService {
    private final ModelRepository modelRepository;
    private final MetricsRepository metricsRepository;

    @Value("${com.ondoset.ai.path}")
	private String scriptPath;

    // 적용된 모델의 정보를 확인
    public GetAdaptModelDTO getAdaptModel() {
        Model model = modelRepository.findByAdaptTrue();

        return new GetAdaptModelDTO(model.getUpdated(), model.getModelVersion(), model.getDataCount());
    }

    public GetModelListDTO getModelList() {
        // findall을 사용하여 모든 모델을 조회
        List<Model> models = modelRepository.findAll();
        List<ModelDTO> modelDTOS = new ArrayList<>();
        for (Model model : models) {
            ModelDTO modelDTO = new ModelDTO(model.getModelId(), model.getModelVersion(), model.getUpdated().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000);
            modelDTOS.add(modelDTO);
        }
        return new GetModelListDTO(modelDTOS);
    }

    public AdaptResponseDTO setAdaptModel(SelectModelDTO selectModelDTO) {
        Long modelId = selectModelDTO.getModelId();
        // 모델을 찾아서 adapt를 true로 변경
        Model model = modelRepository.findById(modelId).orElseThrow(() -> new IllegalArgumentException("모델이 존재하지 않습니다."));
        String script = "prediction.py";
        StringBuilder result = new StringBuilder();
        String lvc = Integer.toString(model.getNumFeatures());
        String cfIter = Integer.toString(model.getIterations());
        String cfLr = Double.toString(model.getLearningRate());
        String cfReg = Double.toString(model.getLambda());
        String cfW = Double.toString(model.getCountWeight());
        log.info( System.getProperty("user.dir"));
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptPath + script, lvc, cfIter, cfLr, cfReg, cfW);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            // 프로세스의 종료를 기다림
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // 프로세스가 비정상적으로 종료된 경우, 오류 로그를 출력
                log.error("Python 프로세스가 오류로 종료되었습니다. 오류 코드: {}", exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Python 프로세스 실행 중 오류가 발생했습니다.", e);
        }

        log.info( result.toString());
        // result (ex:1339)를 이용하여 모델의 데이터 개수를 업데이트
        model.setDataCount(Long.parseLong(result.toString().trim()));
        model.setAdapt(true);
        modelRepository.save(model);
        return new AdaptResponseDTO(model.getModelId());
    }

    public GetCFModelMetricsDTO getCFModelMetrics() {
        // findall을 사용하여 모든 모델을 조회
        List<Model> models = modelRepository.findAll();
        List<CFModelMetricsDTO> cfModelMetricsDTOS = new ArrayList<>();
        for (Model model : models) {
            CFModelMetricsDTO cfModelMetricsDTO = new CFModelMetricsDTO(model.getModelId(), model.getModelVersion(), model.getUpdated(), model.getPrecisionK(), model.getRecallK(), model.getF1ScoreK());
            cfModelMetricsDTOS.add(cfModelMetricsDTO);
        }
        return new GetCFModelMetricsDTO(cfModelMetricsDTOS);
    }

    public GetTrainResultDTO getTrainResult() {
        // adapt가 true인 모델을 찾음
        Model model = modelRepository.findByAdaptTrue();
        List<Metrics> metrics = metricsRepository.findByModelModelId(model.getModelId());
        GetTrainResultDTO trainResultDTO = new GetTrainResultDTO();
        List<OnEpochDTO> onEpochDTOS = new ArrayList<>();
        for (Metrics metric : metrics) {
            OnEpochDTO onEpochDTO = new OnEpochDTO(metric.getType(), metric.getEpoch(), metric.getValue());
            onEpochDTOS.add(onEpochDTO);
        }
        trainResultDTO.setResults(onEpochDTOS);
        return trainResultDTO;
    }

    public TrainModelRespDTO trainModel(TrainModelReqDTO trainModelReqDTO) {
        log.info("trainModel");
        Model lastModel = modelRepository.findFirstByOrderByModelVersionDesc();
        log.info(String.valueOf(trainModelReqDTO.getLvc()), trainModelReqDTO.getCfIter(), trainModelReqDTO.getCfLr(), trainModelReqDTO.getCfReg(), trainModelReqDTO.getCfW());
        String script = "train.py";

        StringBuilder result = new StringBuilder();
        StringBuilder errorResult = new StringBuilder();

        // modelVersion 계산
        BigDecimal lastModelVersion = BigDecimal.valueOf(lastModel.getModelVersion());
        BigDecimal increment = BigDecimal.valueOf(0.2);
        BigDecimal newModelVersion = lastModelVersion.add(increment);

        String version = Double.toString(newModelVersion.doubleValue());
        String lvc = Integer.toString(trainModelReqDTO.getLvc());
        String cfIter = Integer.toString(trainModelReqDTO.getCfIter());
        String cfLr = Double.toString(trainModelReqDTO.getCfLr());
        String cfReg = Double.toString(trainModelReqDTO.getCfReg());
        String cfW = Double.toString(trainModelReqDTO.getCfW());
        log.info(lvc, cfIter, cfLr, cfReg, cfW);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptPath + script, version,  lvc, cfIter, cfLr, cfReg, cfW);
            Process process = processBuilder.start();

            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = outputReader.readLine()) != null) {
                result.append(line).append("\n");
            }
            while ((line = errorReader.readLine()) != null) {
                errorResult.append(line).append("\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("Output: " + result.toString());
        log.error("Error: " + errorResult.toString());
        // JSON 문자열을 파싱
        JsonElement jsonElement = JsonParser.parseString(result.toString());
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        // "train_result" 키에 해당하는 값을 JsonArray로 가져옴
        JsonArray trainResultArray = jsonObject.getAsJsonArray("train_result");

        // JsonArray를 순회하면서 각 JsonObject의 필드 값을 가져옴
        List<OnEpochDTO> onEpochDTOS = new ArrayList<>();
        for (JsonElement element : trainResultArray) {
            JsonObject trainResult = element.getAsJsonObject();
            String type = trainResult.get("type").getAsString();
            int epoch = trainResult.get("epoch").getAsInt();
            double value = trainResult.get("value").getAsDouble();

            OnEpochDTO onEpochDTO = new OnEpochDTO(type, epoch, value);
            onEpochDTOS.add(onEpochDTO);
        }

        // "data_count" 키에 해당하는 값을 가져옴
        Long dataCount = jsonObject.get("data_count").getAsLong();

        // 새로운 모델 객체 생성 및 저장
        Model newModel = new Model();
        newModel.setAdapt(false);
        newModel.setDataCount(dataCount);
        newModel.setModelVersion(newModelVersion.doubleValue());
        newModel.setTrainStatus(true);
        newModel.setDataCount(dataCount);
        newModel.setNumFeatures(trainModelReqDTO.getLvc());
        newModel.setIterations(trainModelReqDTO.getCfIter());
        newModel.setLearningRate(trainModelReqDTO.getCfLr());
        newModel.setLambda(trainModelReqDTO.getCfReg());
        newModel.setCountWeight(trainModelReqDTO.getCfW());
        modelRepository.save(newModel);

        // 새로운 메트릭스 객체 생성 및 저장
        for (OnEpochDTO onEpochDTO : onEpochDTOS) {
            Metrics metrics = new Metrics();
            metrics.setType(onEpochDTO.getType());
            metrics.setEpoch(onEpochDTO.getEpoch());
            metrics.setValue(onEpochDTO.getValue());
            metrics.setModel(newModel);
            metricsRepository.save(metrics);
        }

        return new TrainModelRespDTO(newModel.getModelId());
    }

    public void testModel(TestModelReqDTO testModelReqDTO) {
        Model model = modelRepository.findById(testModelReqDTO.getModelId()).orElseThrow(() -> new IllegalArgumentException("모델이 존재하지 않습니다."));
        String script = "test.py";

        StringBuilder result = new StringBuilder();
        StringBuilder errorResult = new StringBuilder();

        String version = Double.toString(model.getModelVersion());
        String lvc = Integer.toString(model.getNumFeatures());
        String cfIter = Integer.toString(model.getIterations());
        String cfLr = Double.toString(model.getLearningRate());
        String cfReg = Double.toString(model.getLambda());
        String cfW = Double.toString(model.getCountWeight());

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptPath + script, version, lvc, cfIter, cfLr, cfReg, cfW);
            Process process = processBuilder.start();

            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = outputReader.readLine()) != null) {
                result.append(line).append("\n");
            }
            while ((line = errorReader.readLine()) != null) {
                errorResult.append(line).append("\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("Output: " + result.toString());
        log.error("Error: " + errorResult.toString());

        // JSON 문자열을 파싱
        JsonElement jsonElement = JsonParser.parseString(result.toString());
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        double loss = jsonObject.get("loss").getAsDouble();
        // "precision" 키에 해당하는 값을 가져옴
        double precision = jsonObject.get("precision").getAsDouble();
        // "recall" 키에 해당하는 값을 가져옴
        double recall = jsonObject.get("recall").getAsDouble();
        // "f1_score" 키에 해당하는 값을 가져옴
        double f1Score = jsonObject.get("f1_score").getAsDouble();

        // 모델 객체 업데이트
        model.setLoss(loss);
        model.setPrecisionK(precision);
        model.setRecallK(recall);
        model.setF1ScoreK(f1Score);
        modelRepository.save(model);
    }


}
