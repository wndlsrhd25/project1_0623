package com.yedam.app.management;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import com.yedam.app.member.MemberDAO;
import com.yedam.app.offender.Offender;
import com.yedam.app.offender.OffenderDAO;
import com.yedam.app.prison.PrisonDAO;

public class ManagementOffender {

	// 필드
	protected Scanner sc = new Scanner(System.in);
	protected MemberDAO mDAO = MemberDAO.getInstance();
	protected OffenderDAO oDAO = OffenderDAO.getInstance();
	protected PrisonDAO pDAO = PrisonDAO.getInstance();
	protected ManagementDAO gDAO = ManagementDAO.getInstance();

	// 생성자
	public void run() {

		while (true) {
			menuPrint();

			int menuNo = menuSelect();

			if (menuNo == 1) {
				// 1.전체조회
				searchOffender();
			} else if (menuNo == 2) {
				// 2.등록
				insertOffender();
			} else if (menuNo == 3) {
				// 3.수정 - 형량,지역
				updateOffender();
			} else if (menuNo == 4) {
				// 4.삭제
				deleteOffender();
			} else if (menuNo == 5) {
				// 5. 지역별 전체 조회
				selectLocaiont();
			} else if (menuNo == 6) {
				// 6. 단건조회(투옥중인 사람)
				selectImprison();
			} else if (menuNo == 7) {
				// 6. 단건조회(출소한 사람)
				selectFreedom();
			} else if (menuNo == 9) {
				// 프로그램 종료
				exit();
				break;
			} else {
				// 입력오류
				showInputError();
			}
		}
	}

	protected void menuPrint() {
		System.out.println("\n=====================================================================================================");
		System.out.println("1.전체조회 2.등록 3.형량 및 지역 수정 4.가석방 및 삭제 5.지역별 조회 6.수감중인 사람 7.출소한 사람 9.메인으로");
		System.out.println("=====================================================================================================");

	}

	protected int menuSelect() {
		int menuNo = 0;
		try {
			menuNo = Integer.parseInt(sc.nextLine());

		} catch (NumberFormatException e) {
			System.out.println("숫자를 입력해주시기 바랍니다.");
		}
		return menuNo;
	}

	protected void exit() {
		System.out.println("메인으로 돌아갑니다.");
	}

	protected void showInputError() {
		System.out.println("메뉴에서 입력해주시기 바랍니다.");
	}

	// 1. 전체조회
	protected void searchOffender() {
		List<Management> list = gDAO.selectAll();

		for (Management management : list) {
			System.out.println(management);
		}
	}

	// 2. 범죄자등록
	protected void insertOffender() {
		// 2-1. 정보 입력
		Offender offender = inputOffender();

		if(offender != null) {
			// 디비에 저장
			oDAO.insert(offender);
		} else {
			System.out.println("등록이 실패되었습니다.");
		}
	}

	//2-1 정보입력
	protected Offender inputOffender() {
		Offender info = null;

		try {
			info = new Offender();
			System.out.print("이름 > ");
			info.setName(sc.nextLine());
			System.out.print("성별 > ");
			info.setGender(sc.nextLine());
			System.out.print("생년월일(yyyy-mm-dd) > ");
			info.setBirth(Date.valueOf(sc.nextLine()));
			System.out.print("주소 > ");
			info.setLocation(sc.nextLine());
			System.out.print("범죄명 > ");
			info.setCrime(sc.nextLine());
			System.out.print("수감일(yyyy-mm-dd) > ");
			
			//미래 날짜 입력 금지
			Date temp = Date.valueOf(sc.nextLine());
			if(temp.after(java.sql.Date.valueOf(LocalDate.now()))) {
				System.out.println("입력 날짜를 잘 확인하세요");
				return null;
			}else {
				info.setImprison(temp);
			}			
				
			System.out.print("형량(n년 n개월) > ");
			String str = sc.nextLine();

			try {
				// 오류 X >> 숫자가 들어갔을때 실행됨
				int num = Integer.parseInt(str);
				System.out.println("괄호의 형태대로 입력해주세요.");
				return null;

			} catch (NumberFormatException e) {

				// 문자형태로 들어가면 오류가 남 예외처리를 해버림
				long sentence = calcSentence(str);
				// 만약에 ㅇㅈㅇㅊ이런 문자를 넣으면 sentence값이 0이 나오니까 null을 반환함
				if (sentence == 0) {
					System.out.println("괄호의 형태대로 입력해주세요.");
					return null;
				} else {
					// 제대로된 값이 들어가면 원래대로 계산이됨
					info.setSentence(sentence);
					System.out.println("정상적으로 등록되었습니다.");
				}
				return info;
			}
			
		} catch (IllegalArgumentException e) {
			// 왜 정상적으로 실행합니다가 뜰까?
			System.out.println("괄호의 형태대로 입력해주세요.");

			info = null;
		}
		return info;

	}

	// 형량 달로 바꾸기
	private long calcSentence(String str) {

		StringTokenizer st = new StringTokenizer(str);
		long month = 0;
		try {
			while (st.hasMoreTokens()) {
				String y = st.nextToken();
				if (y.charAt(y.length() - 1) == '년') {
					y = y.substring(0, y.length() - 1);
					month += (Integer.parseInt(y) * 12);

				} else if (y.charAt(y.length() - 1) == '월') {
					y = y.substring(0, y.length() - 2);
					month += Integer.parseInt(y);
				}
			}
		} catch (NumberFormatException e) {
			//이건 그럼 0값이 들어가니까
			return month;
		}
		//이건 계산된 값이 들어가겠지???
		return month;
	}

	// 3.형량 수정
	private void updateOffender() {
		// 이름 검색
		String name = inputName();

		// 동명이인 존재여부 확인
		List<Management> list = gDAO.selectName(name);
		Management management;
		if (list.size() == 0) {
			System.out.println("범죄자가 존재하지 않습니다.");
			return;
		}
		// 죄수 번호 검색
		if (list.size() > 1) {
			System.out.println("\n동명이인이 존재합니다. 죄수번호를 확인해주세요");
			for (Management m : list) {
				System.out.println(m);
			}
			int prisonNum = inputPrisonNum();

			// 죄수 번호로 검색
			management = gDAO.selectPrisonNum(prisonNum);
		} else {
			management = list.get(0);
		}

		// (1)수정할 정보 입력(형량)

		Management temp = inputUpdateSentence(management);

		// 디비에 연결
		if (temp != null) {
			management = temp;
			oDAO.updateSentence(management);
		}

		// new java.util.Date()); //오늘 객체를 생성한다.
		// 현재 날짜를 yyyy-mm-dd형식으로 바꿈
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		// 이걸 다시 문자열로 바꾼것
		String ss = sdf.format(new java.util.Date());
		// sql.date타입으로 변환
		Date d = Date.valueOf(ss);

		if (d.after(management.getReleased())) {
			System.out.println("석방되었습니다.");
			management.setFreedom("출소");
			gDAO.updateFreedom(management);
		} else {
			management.setFreedom("수감중");
			gDAO.updateFreedom(management);
		}

		// (2) 수정할 정보 입력(지역)
		management = inputUpdateLocation(management);

		// 디비에 연결
		if (management != null) {
			oDAO.updateLocation(management);

		}

	}

	// 이름 검색
	private String inputName() {
		System.out.print("\n이름을 입력하세요 > ");
		return sc.nextLine();
	}

	// 죄수 번호 검색
	private int inputPrisonNum() {
		System.out.println();
		System.out.print("죄수번호를 입력하세요 > ");
		return Integer.parseInt(sc.nextLine());
	}

	// 수정할 형량 입력
	private Management inputUpdateSentence(Management management) {
		String fromSentenceToString = management.getSentence() / 12 + "년 " + management.getSentence() % 12 + "개월";
		System.out.println("\n기존 형량 > " + fromSentenceToString);
		System.out.print("수정할 형량(n년 n개월 - 원치 않을 경우 0 입력)>");

		// 예외처리를 해야함 n년 n개 형태로 받아야하는데 013같은 숫자를를 넣으면 숫자예외처리를 해야함
		// 원래 0이면 수정하지 않음으로 했는데 했눈데 -1이 나을까요...?
		// 문자만 넣으면 어차피 sentence(형량을 달로 뽑아오는 메소드)에 값이 0이 되기 떄문에(뽑아올 수가 없음) 이것도 예외처리해야함

		String sentenceString = sc.nextLine();

		//먼저 0이 들어오는것 부터 처리함
		if (sentenceString.equals("0")) {
			System.out.println("수정을 취소하였습니다.");
			return null;
			
			//년 개월이라는 글자가 있는 경우에만 실행하고 싶음 (ㅁㅇㄴㄹㄴㅇㅁ이런 문자로 안됨)
		} else if (sentenceString.contains("년") || sentenceString.contains("개월")) {
			// 형량수정하는거 실행
			long sentence = calcSentence(sentenceString);
			//만약에 개월년,년월 등 형식에 맞지 않은 수를 입력하면 0이 리턴되게 만들었음
			if (sentence == 0) {
				System.out.println("형식에 맞게 입력하세요");
				return null;
			} else {
				management.setSentence(sentence);
				System.out.println("정상적으로 수정되었습니다.");
				return management;
			}

		} else {
			// 문자로 쓰면 이렇게 나옴
			System.out.println("n년 n개월 형식으로 입력하세요");
			return null;
		}

	}
	/*
	 * //이게 은경씨가 해준거 if (sentenceString.equals("0")) {
	 * System.out.println("수정을 취소하였습니다."); return null; } else
	 * if(sentenceString.contains("년월")|| sentenceString.contains("개월 년") ||
	 * sentenceString.contains("년 개월") ||sentenceString.contains("년 개 월")||
	 * sentenceString.contains("년개월")|| sentenceString.contains("년 월")||
	 * sentenceString.contains("월 년")|| sentenceString.contains("개월 년")){
	 * System.out.println("고만해라"); return null; } else if
	 * (sentenceString.contains("년") || sentenceString.contains("개월")) { // 수정하는거 실행
	 * long sentence = calcSentence(sentenceString);
	 * 
	 * //내 생각으로는 년월이 들어가면 저기 계산이 아무것도 안되서 0값이 나오지 않을까 했음 if (sentence == 0) {
	 * System.out.println("형식에 맞게 입력하세요"); return null; } else {
	 * management.setSentence(sentence); System.out.println("정상적으로 수정되었습니다.");
	 * return management; }
	 * 
	 * } else { // 문자로 쓰면 이렇게 나옴 System.out.println("n년 n개월 형식으로 입력하세요"); return
	 * null; }
	 * 
	 * }
	 */
	/*
	 * try { int num = Integer.parseInt(sentenceString);
	 * System.out.println("++++괄호의 형태로 입력해주세요++++"); return null; } catch
	 * (NumberFormatException e) {
	 * 
	 * long sentence = calcSentence(sentenceString); // 만약 문자가 민아ㅓ램 넣으면 sentence 값이
	 * 0이니까 null을 반환함 if (sentence == 0) {
	 * System.out.println("++++괄호의 형태대로 입력해주세요++++"); return null; } else if
	 * (!sentenceString.equals("0")) { // 제대로된 값이 들어가면 원래대로 계산함
	 * management.setSentence(sentence); System.out.println("정상적으로 수정되었습니다."); }
	 * else { System.out.println("+++++괄호의 형태대로 입력해주세요++++");
	 * System.out.println("수정에 실패했습니다."); } return management; } }
	 */

	/*
	 * try { if(zero !=-1) { management.setSentence(zero);
	 * System.out.println("형량 수정이 완료되었습니다."); return management; }
	 * System.out.println("괄호의 형태로 입력해1"); } catch(NumberFormatException e) { // 문자가
	 * 들어가면 오류남 예외처리를 해줘야해 //이게 sentence 형량을 달로 뽑아놓는 메소드 long sentence =
	 * calcSentence(sentenceString); if(sentence == 0) {
	 * System.out.println("++++괄호의 형태대로 입력해주세요++++"); return null; } else {
	 * System.out.println("+++++괄호의 형태대로 입력해주세요3");
	 * System.out.println("수정에 실패했습니다."); } return null; } }
	 * 
	 * 
	 * try { int num = Integer.parseInt(sentenceString);
	 * System.out.println("++++괄호의 형태로 입력해주세요++++"); return null; } catch
	 * (NumberFormatException e) {
	 * 
	 * long sentence = calcSentence(sentenceString); // 만약 문자가 민아ㅓ램 넣으면 sentence 값이
	 * 0이니까 null을 반환함 if (sentence == 0) {
	 * System.out.println("++++괄호의 형태대로 입력해주세요++++"); return null; } else if
	 * (!sentenceString.equals("0")) { // 제대로된 값이 들어가면 원래대로 계산함
	 * management.setSentence(sentence); System.out.println("정상적으로 수정되었습니다."); }
	 * else { System.out.println("+++++괄호의 형태대로 입력해주세요++++");
	 * System.out.println("수정에 실패했습니다."); } return management; }
	 */

	// 수정할 지역 입력
	private Management inputUpdateLocation(Management management) {

		List<Management> distinct = gDAO.distinctPrisonLocation();
		List<String> prisonLocation = new ArrayList<String>();

		for (Management mng : distinct) {
			prisonLocation.add(mng.getPrisonLocation());
		}
		System.out.println("기존 지역 > " + management.getPrisonLocation());
		System.out.println("변경 가능한 지역 > " + distinct.get(0).getPrisonLocation());
		System.out.print("변경할 지역(원치 않을 경우 0 입력) > ");
		String location = sc.nextLine();

		if (location.contains("0")) {
			System.out.println("변경을 취소하였습니다.");
			return null;
		} else if (!prisonLocation.contains(location)) {
			System.out.println("변경할 수 없는 지역입니다.");
			return null;
		} else {
			management.setPrisonLocation(location);
		}
		return management;
	}

	// 4.삭제 -완전삭제인지, 가석방인지
	private void deleteOffender() {

		// (1)완전삭제 (2)가석방 선택하기
		int select = selectNum();
		if (select != 1 && select != 2) {
			System.out.println("메뉴에 없는 숫자입니다.");
			return;
		}

		// (1)완전삭제
		if (select == 1) {
			// 이름 검색
			String name = inputName();

			// 동명이인 존재여부 확인
			List<Management> list = gDAO.selectName(name);
			Management management;
			if (list.size() == 0) {
				System.out.println("범죄자가 존재하지 않습니다.");
				return;
			}
			if (list.size() > 1) {
				System.out.println("동명이인이 존재합니다. 죄수번호를 확인해주세요");
				for (Management m : list) {
					System.out.println(m);
				}
				int prisonNum = inputPrisonNum();

				// 죄수 번호로 검색
				management = gDAO.selectPrisonNum(prisonNum);
			} else {
				management = list.get(0);
			}

			System.out.println("정상적으로 삭제 되었습니다.");
			gDAO.delete(management);

			// (2) 가석방
		} else if (select == 2) {

			// 이름 검색
			String name = inputName();

			// 동명이인 존재여부 확인
			List<Management> list = gDAO.selectName(name);
			Management management;

			if (list.size() == 0) {
				System.out.println("범죄자가 존재하지 않습니다.");
				return;
			}

			if (list.size() > 1) {
				System.out.println("\n동명이인이 존재합니다. 죄수번호를 확인해주세요");
				for (Management m : list) {
					System.out.println(m);
				}
				int prisonNum = inputPrisonNum();

				// 죄수 번호로 검색해서 구분
				management = gDAO.selectPrisonNum(prisonNum);
			} else {
				management = list.get(0);
			}

			// 수정할 정보 입력(가석방)
			management = inputParole(management);

			if (management == null) {
				return;
			}

			// 디비에 연결
			gDAO.updateSentence(management);
			// 여기까지 실행됨, 디비에는 새로운 값이 저장된 상태

			// 보에 업데이트된 정보를 새로 가져와야함(prisonNum은 가석방 된 한사람의 단건조회 데려온것)
			Management parole = gDAO.selectPrisonNum(management.getPrisonNum());

			if (parole.getSentence() == 0) {
				System.out.println("가석방되었습니다.");
				parole.setFreedom("가석방");
				gDAO.updateFreedom(parole);
			}
		}
		return;
	}

	// (1)완전 삭제 할지 (2)가석방할지 선택
	private int selectNum() {
		int selectNum;
		System.out.println("원하는 메뉴를 선택하세요");
		System.out.println("1.완전삭제 2.가석방");
		System.out.print("(1 or 2) > ");

		try {
			selectNum = Integer.parseInt(sc.nextLine());

		} catch (NumberFormatException e) {
			// 문자 넣으면 여기로옴
			System.out.println("괄호의 형태로 입력해주세요\n");
			return selectNum();
		}
		return selectNum;
	}

	// 가석방 여부
	private Management inputParole(Management management) {
		String fromSentenceToString = management.getSentence() / 12 + "년 " + management.getSentence() % 12 + "개월";
		System.out.println("\n기존 형량> " + fromSentenceToString);
		System.out.print("가석방을 원할 경우 1(원치 않을 경우 2 입력)>");
		try {
			int sentenceNum = Integer.parseInt(sc.nextLine());

			if (sentenceNum ==1) {
				gDAO.updateRealesed(management);
			} else if(sentenceNum ==2) {
				System.out.println("가석방을 취소하였습니다.");
				return null;
			} else {
				System.out.println("메뉴에 없는 숫자입니다.");
				System.out.println("1또는 2를 입력해주세요.");
			}

		} catch (NumberFormatException e) {
			System.out.println("숫자로 입력해 주세요");
			return null;

		}
		return management;

	}

	// 5. 지역별 조회
	private void selectLocaiont() {
		// 지역 입력
		String prisonLocation = inputLocation();

		List<Management> list = gDAO.selectPrisonLocation(prisonLocation);
		
		if (list == null) {
			System.out.println("검색가능한 지역이 아닙니다.");
		}
		System.out.println("해당 지역에 범죄자 정보입니다.");

		// 수용중인 인원
		for (Management management : list) {
			System.out.println(management);
		}
	}

	// 지역 입력
	private String inputLocation() {
		//범죄자가 있는 지역만 중복 없이 구분해서 list에 담음
		List<Management> list = gDAO.distinctOffenderLocation();

		if (list.size() == 0) {
			System.out.println("검색 가능한 지역이 없다");
			return "";
		}

		System.out.print("검색 가능한 지역 - " + list.get(0).getPrisonLocation());
		for (int i = 1; i < list.size(); i++) {
			System.out.print("/" + list.get(i).getPrisonLocation());
		}
		System.out.print("\n지역 > ");
		return sc.nextLine();
	}

	// 6. 수감중인 사람 조회
	private void selectImprison() {

		List<Management> list = gDAO.selectImprison("수감중");

		if (list.size() == 0) {
			System.out.println("수감중인 사람이 없습니다.");
			return;
		}

		System.out.println("수감자 정보입니다.");

		// 수감자출력
		for (Management management : list) {
			System.out.println(management);

		}
	}

	// 7. 출소한 사람 조회
	private void selectFreedom() {

		List<Management> list = gDAO.selectFreedom("출소","출소예정", "가석방");

		if (list.size() == 0) {
			System.out.println("출소한 사람이 없습니다.");
			return;
		}
		System.out.println("출소자 정보입니다.");
		// 출소자 출력
		for (Management management : list) {
			System.out.println(management);
		}
	}
	
}
