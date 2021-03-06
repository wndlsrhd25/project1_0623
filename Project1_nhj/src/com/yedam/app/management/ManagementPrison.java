package com.yedam.app.management;


import java.util.List;
import java.util.Scanner;

import com.yedam.app.member.MemberDAO;
import com.yedam.app.offender.OffenderDAO;
import com.yedam.app.prison.Prison;
import com.yedam.app.prison.PrisonDAO;

public class ManagementPrison {

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
				// 1.등록
				insertPrison();
			} else if (menuNo == 2) {
				// 2. 전체조회
				selectAll();
			} else if (menuNo == 3) {
				// 3. 단건 조회 - 지역별 //혹시 그 지역에 있는 죄수자들도 함께 나오게 할 수 있을까???
				selectLocation();
			} else if (menuNo == 4) {
				// 4. 교도소 이름 변경
				updatePrisonName();
			} else if (menuNo == 5) {
				// 5. 수용 가능한 인원 수정
				updateAccommodate();
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
		System.out.println("\n===============================================================================");
		System.out.println("1.교도소 등록 2.전체조회 3.지역별 조회 4.교도소이름 변경 5.수용인원 변경 9.뒤로가기");
		System.out.println("===============================================================================");

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

	// 1.교도소 등록
	private void insertPrison() {
		// 교도소 정보 입력
		Prison info = inputPrison();

		if (info == null) {
			return;
		}

		// 해당 지역 교도소 등록 여부
		Prison prison = pDAO.selectLocation(info.getPrisonLocation());

		if (prison != null) {
			System.out.println("해당 지역에 교도소가 존재합니다.");
			return;
		}

		// db에 저장
		pDAO.insert(info);
	}

	private Prison inputPrison() {
		Prison info = new Prison();
		System.out.print("교도소 이름 > ");
		info.setPrisonName(sc.nextLine());
		System.out.print("교도소 지역 > ");
		info.setPrisonLocation(sc.nextLine());
		try {
			System.out.print("수용가능한 인원 > ");
			info.setPrisonAccommodate(Integer.parseInt(sc.nextLine()));
		} catch (NumberFormatException e) {
			System.out.println("숫자를 입력하세요");
			return null;
		}
		return info;
	}

	// 2.전체조회
	private void selectAll() {
		List<Prison> list = pDAO.selectAll();

		for (Prison prison : list) {
			// System.out.println(prison);
			int people = pDAO.selectInfo(prison.getPrisonLocation());
			int freedomNum = pDAO.selectFreedom(prison.getPrisonLocation());
			//수용 가능한 인원은 최대 수용인원에서 -수감된 인원 + 석방된 인원 + 가석방된 인원
			prison.setPrisonOccupy(prison.getPrisonAccommodate() - people + freedomNum);
			pDAO.updateOccupy(prison);
			System.out.println(prison);
			

		}
	}

	
	// 3.지역별 조회
	private void selectLocation() {
		// 3-1지역 입력
		String prisonLocation = inputLocation();

		Prison prison = pDAO.selectLocation(prisonLocation);

		if (prison == null) {
			System.out.println("검색가능한 지역이 아닙니다.");
			return;
		}
		
		
		//지역 교도소 정보 출력하고 싶음 - 근데 석방여부에 따른 수용인원이 변경되어야함 위에 메소드 참고
			int people = pDAO.selectInfo(prison.getPrisonLocation());
			int freedomNum = pDAO.selectFreedom(prison.getPrisonLocation());
			prison.setPrisonOccupy(prison.getPrisonAccommodate() - people + freedomNum);
			pDAO.updateOccupy(prison);
			System.out.println(prison);
		
		

		// 그 지역에 따른 범죄자 리스트도 같이 출력하고 싶음
		List<Management> offender = gDAO.selectPrisonLocation(prisonLocation);
		try {
			if (offender == null) {
				System.out.println("\n해당지역에 수감된 범죄자가 없습니다.");
			}
			System.out.println("해당지역에 수감된 범죄자입니다.");

			for (Management offenderList : offender) {
				System.out.println(offenderList);
			}
		} catch (NullPointerException e) {
			System.out.println("해당지역에 수감자가 없습니다.");
		}

	}

	// 3-1지역 검색
	private String inputLocation() {
		List<Prison> list = pDAO.selectAll();
		// 리스트 0번 부터 시작하니까 예를들면 서울 정보가 들어가있음
		System.out.print("검색 가능한 지역 - " + list.get(0).getPrisonLocation());
		for (int i = 1; i < list.size(); i++) {
			System.out.print("/" + list.get(i).getPrisonLocation());

		}
		System.out.print("\n지역 > ");
		return sc.nextLine();
	}

	// 4. 교도소 이름 변경
	private void updatePrisonName() {
		// 교도소 지역 검색
		String prisonLocation = inputLocation();

		// 교도소 정보 검색
		Prison prison = pDAO.selectLocation(prisonLocation);

		if (prison == null) {
			System.out.println("등록된 교도소 정보가 없습니다.");
			return;
		}

		// 수정할 정보 입력
		prison = inputUpdateName(prison);

		// db에 저장
		pDAO.updateName(prison);
	}

	
	// 5.수용인원 수정
	private void updateAccommodate() {
		// 교도소 지역 입력
		String prisonLocation = updateLocation();

		// 교도소 정보 검색
		Prison prison = pDAO.selectLocation(prisonLocation);

		if (prison == null) {
			System.out.println("변경 가능한 지역이 아닙니다.");
			return;
		}
		// 수정할 정보 입력
		prison = inputUpdateInfo(prison);

		// db에 수정
		pDAO.updateAccommodate(prison);
		pDAO.updateOccupy(prison);
	}

	// 변경 지역 조회
	private String updateLocation() {
		List<Prison> list = pDAO.selectAll();
		// 리스트 0번 부터 시작하니까 예를들면 서울 정보가 들어가있음
		System.out.print("변경 가능한 지역 - " + list.get(0).getPrisonLocation());

		for (int i = 1; i < list.size(); i++) {
			System.out.print("/" + list.get(i).getPrisonLocation());

		}
		System.out.print("\n지역 > ");
		return sc.nextLine();
	}

	// 수용인원 변경 여부
	private Prison inputUpdateInfo(Prison prison) {
		System.out.println("기존 최대수용 인원 > " + prison.getPrisonAccommodate());
		System.out.print("변경 인원(원치 않을 경우 0 입력) > ");

		try {
			int accommodate = Integer.parseInt(sc.nextLine());
			int occupy = pDAO.selectInfo(prison.getPrisonLocation());

			if (accommodate != 0) {
				System.out.println("수정이 완료되었습니다.");
				prison.setPrisonAccommodate(accommodate);
				prison.setPrisonOccupy(accommodate - occupy);
			} else {
				System.out.println("수정이 되지 않았습니다.");
			}
			return prison;

		} catch (NumberFormatException e) {
			System.out.println("숫자를 입력해주시기 바랍니다.");
		}
		return prison;
	

	}

	// 교도소 이름 변경 여부
	private Prison inputUpdateName(Prison prison) {
		System.out.println("기존 교도소 이름 > " + prison.getPrisonName());
		System.out.print("변경할 이름(원치 않을 경우 0 입력) > ");
		String name = sc.nextLine();
		if (!name.equals("0")) {
			System.out.println("수정이 완료되었습니다.");
			prison.setPrisonName(name);
		} else {
			System.out.println("수정이 되지 않았습니다.");
		}
		return prison;
	}

}
