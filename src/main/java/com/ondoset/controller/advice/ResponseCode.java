package com.ondoset.controller.advice;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {

	COM2000(200, "common_2000", "요청 처리에 성공했습니다. "),
	COM4000(400, "common_4000", "잘못된 요청입니다. "),
	COM4001(400, "common_4001", "Type Error: "),
	AUTH4000(400, "auth_4000", "JWT 토큰이 만료되었습니다. "),
	AUTH4001(400, "auth_4001", "JWT 토큰이 잘못되었거나, 유효하지 않습니다. "),
	COM4010(401, "com_4010", "해당 기능은 인증이 필요합니다. 인증 정보가 비어 있습니다. "),
	AUTH4010(401, "auth_4010", "아이디 또는 비밀번호가 잘못되었습니다. "),
	COM4030(403, "common_4030", "접근이 금지된 요청입니다. "),
	COM4040(404, "common_4040", "요청한 리소스를 찾을 수 없습니다. "),
	DB4040(404, "db_4040", "요청한 db 요청을 찾을 수 없습니다. "),
	AI4040(404, "ai_4040", "요청한 ai 요청을 찾을 수 없습니다. "),
	COM4090(409, "common_4090", "중복된 데이터가 이미 존재합니다. "),
	COM4091(409, "common_4091", "삭제된 엔티티를 수정할 수 없습니다. "),
	COM4130(413, "common_4130", "요청된 페이로드 용량이 너무 큽니다. "),
	COM4150(415, "common_4150", "지원하지 않는 미디어 타입입니다. "),
	COM4290(429, "common_4290", "요청이 제한된 양을 초과하였습니다. "),
	COM5000(500, "common_5000", "서버에서 처리 중 오류가 발생하였습니다. "),
	AI5000(500, "ai_5000", "AI 모델 학습 중 오류가 발생하였습니다. "),
	AI5001(500, "ai_5001", "AI 서비스 I/O 데이터 타입에 오류가 발생하였습니다. "),
	AI5002(500, "ai_5002", "AI 모델 예측 중 오류가 발생하였습니다. "),
	AI5003(500, "ai_5003", "AI 모델 구성환경 설정에 오류가 발생하였습니다. "),
	DB5000(500, "db_5000", "DB 접근 중 오류가 발생하였습니다. "),
	COM5020(502, "common_5020", "서버가 현재 점검 중입니다. ");

	private final int status;
	private final String code;
	private final String message;

}
