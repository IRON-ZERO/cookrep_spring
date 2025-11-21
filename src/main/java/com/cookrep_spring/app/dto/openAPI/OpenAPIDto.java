package com.cookrep_spring.app.dto.openAPI;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAPIDto {
	@JsonProperty("RCP_SEQ")
	private String rcpSeo;//일련번호

	@JsonProperty("RCP_NM")
	private String rcpNm;// 메뉴명

	@JsonProperty("RCP_WAY2")
	private String rcpWay2;// 조리방법

	@JsonProperty("RCP_PAT2")
	private String rcpPat2; // 요리종류

	@JsonProperty("INFO_WGT")
	private String infoWgt; // 중량(1인분)

	@JsonProperty("INFO_ENG")
	private String infoEng; // 열량

	@JsonProperty("INFO_CAR")
	private String infoCar; // 탄수화물

	@JsonProperty("INFO_PRO")
	private String infoPro; // 단백질

	@JsonProperty("INFO_FAT")
	private String infoFat; // 지방

	@JsonProperty("INFO_NA")
	private String infoNa; // 나트륨

	@JsonProperty("HASH_TAG")
	private String hashTag; // 해쉬태그

	@JsonProperty("ATT_FILE_NO_MAIN")
	private String attFileNoMain; // 이미지경로(소)

	@JsonProperty("ATT_FILE_NO_MK")
	private String attFileNoMk; // 이미지경로(대)

	@JsonProperty("RCP_PARTS_DTLS")
	private String rcpPartsDtls; // 재료정보

	@JsonProperty("MANUAL01")
	private String manual01; //	만드는법_01

	@JsonProperty("MANUAL_IMG01")
	private String manualImg01; //	만드는법_이미지_01

	@JsonProperty("MANUAL02")
	private String manual02; //	만드는법_02

	@JsonProperty("MANUAL_IMG02")
	private String manualImg02; // 만드는법_이미지_02

	@JsonProperty("MANUAL03")
	private String manual03; // 만드는법_03

	@JsonProperty("MANUAL_IMG03")
	private String manualImg03; // 만드는법_이미지_03

	@JsonProperty("MANUAL04")
	private String manual04;//	만드는법_04

	@JsonProperty("MANUAL_IMG04")
	private String manualImg04; //	만드는법_이미지_04

	@JsonProperty("MANUAL05")
	private String manual05;//	만드는법_05

	@JsonProperty("MANUAL_IMG05")
	private String manualImg05; //	만드는법_이미지_05

	@JsonProperty("MANUAL06")
	private String manual06;//	만드는법_06

	@JsonProperty("MANUAL_IMG06")
	private String manualImg06; //	만드는법_이미지_06

	@JsonProperty("MANUAL07")
	private String manual07;//	만드는법_07

	@JsonProperty("MANUAL_IMG07")
	private String manualImg07; //	만드는법_이미지_07

	@JsonProperty("MANUAL08")
	private String manual08;// 만드는법_08

	@JsonProperty("MANUAL_IMG08")
	private String manualImg08; //	만드는법_이미지_08

	@JsonProperty("MANUAL09")
	private String manual09;// 만드는법_09

	@JsonProperty("MANUAL_IMG09")
	private String manualImg09; //	만드는법_이미지_09

	@JsonProperty("MANUAL10")
	private String manual10;//	만드는법_10

	@JsonProperty("MANUAL_IMG10")
	private String manualImg10; //	만드는법_이미지_10

	@JsonProperty("MANUAL11")
	private String manual11;// 만드는법_11

	@JsonProperty("MANUAL_IMG11")
	private String manualImg11; //	만드는법_이미지_11

	@JsonProperty("MANUAL12")
	private String manual12;// 만드는법_12

	@JsonProperty("MANUAL_IMG12")
	private String manualImg12; //	만드는법_이미지_12

	@JsonProperty("MANUAL13")
	private String manual13; // 만드는법_13

	@JsonProperty("MANUAL_IMG13")
	private String manualImg13; //	만드는법_이미지_13

	@JsonProperty("MANUAL14")
	private String manual14; // 만드는법_14

	@JsonProperty("MANUAL_IMG14")
	private String manualImg14; //	만드는법_이미지_14

	@JsonProperty("MANUAL15")
	private String manual15; //만드는법_15

	@JsonProperty("MANUAL_IMG15")
	private String manualImg15;//	만드는법_이미지_15

	@JsonProperty("MANUAL16")
	private String manual16;//	만드는법_16

	@JsonProperty("MANUAL_IMG16")
	private String manualImg16;//	만드는법_이미지_16

	@JsonProperty("MANUAL17")
	private String manual17;//	만드는법_17

	@JsonProperty("MANUAL_IMG17")
	private String manualImg17;//	만드는법_이미지_17

	@JsonProperty("MANUAL18")
	private String manual18;//	만드는법_18

	@JsonProperty("MANUAL_IMG18")
	private String manualImg18;//	만드는법_이미지_18

	@JsonProperty("MANUAL19")
	private String manual19;//	만드는법_19

	@JsonProperty("MANUAL_IMG19")
	private String manualImg19;//	만드는법_이미지_19

	@JsonProperty("MANUAL20")
	private String manual20;//	만드는법_20

	@JsonProperty("MANUAL_IMG20")
	private String manualImg20;//	만드는법_이미지_20

	@JsonProperty("RCP_NA_TIP")
	private String rcpNaTip;//저감 조리법 TIP

}