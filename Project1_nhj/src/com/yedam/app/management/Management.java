package com.yedam.app.management;

import java.sql.Date;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class Management {

	public String prisonName;
	public String prisonLocation;
	public int prisonNum;
	public String name;
	public String gender;
	public Date birth;
	public String crime;
	public Date imprison;
	public long sentence;
	public Date released;
	public String freedom;

	public void freedomCheck() {
		
	}

	@Override
	public String toString() {
		long year = getSentence() / 12;
		long month = getSentence() % 12;
		String info = "";

		if (year == 0) {
			// 0개월일 경우
			info = "죄수번호:" + prisonNum + " 이름:" + name + " 성별:" + gender + " 생년월일:" + birth + " " + prisonName + " 지역:"
					+ prisonLocation + " 죄목:" + crime + " 수감일:" + imprison + " 형량:" + month + "개월" + " 석방일:" + released;
			if (released.before(Date.valueOf(LocalDate.now()))) {
				freedom = " 출소";
			} else if(sentence ==0) {
				freedom = " 가석방";
			}else if (released.equals(Date.valueOf(LocalDate.now()))){
				freedom = " 출소예정";
			}else {
				freedom = " 수감중";
			}
			info += freedom;

			System.out.println();
		} else if (month == 0) {
			info = "죄수번호:" + prisonNum + " 이름:" + name + " 성별:" + gender + " 생년월일:" + birth + " " + prisonName + " 지역:"
					+ prisonLocation + " 죄목:" + crime + " 수감일:" + imprison + " 형량:" + year + "년" + " 석방일:" + released;
			if (released.before(Date.valueOf(LocalDate.now()))) {
				freedom = " 출소";
			}else if(sentence ==0) {
				freedom = " 가석방";
			}else if (released.equals(Date.valueOf(LocalDate.now()))){
				freedom = " 출소예정";
			}else {
				freedom = " 수감중";
			}
			info += freedom;
			System.out.println();
		} else {
			info = "죄수번호:" + prisonNum + " 이름:" + name + " 성별:" + gender + " 생년월일:" + birth + " " + prisonName + " 지역:"
					+ prisonLocation + " 죄목:" + crime + " 수감일:" + imprison + " 형량:" + year + "년 " + month + "개월"
					+ " 석방일:" + released;
			if (released.before(Date.valueOf(LocalDate.now()))) {
				freedom = " 출소";
			}else if(sentence ==0) {
				freedom = " 가석방";
			}else if (released.equals(Date.valueOf(LocalDate.now()))){
				freedom = " 출소예정";
			}else {
				freedom = " 수감중";
			}
			info += freedom;
			System.out.println();
		}

		return info;
	}

	public String allView() {
		String info = "\n죄수번호:" + prisonNum + " 이름:" + name + " 성별:" + gender + " 생년월일:" + birth + " 죄목:" + crime + " "
				+ prisonName + " 지역:" + " " +prisonLocation;
	
		if (released.before(Date.valueOf(LocalDate.now()))) {
			freedom = " 출소";
		} else if (released.equals(Date.valueOf(LocalDate.now()))){
			freedom = " 출소예정";
		}else {
			freedom = " 수감중";
		}
		return info += freedom;

	}

}
