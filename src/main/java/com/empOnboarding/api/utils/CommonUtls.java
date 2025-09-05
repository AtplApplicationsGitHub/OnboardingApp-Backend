package com.empOnboarding.api.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.security.UserPrincipal;
import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.DiffResult;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
public class CommonUtls {

	private static ModelMapper modelMapper = null;

	public static boolean trueIfOne(int value) {
		return value == 1;
	}

	public static int OneIfTrue(boolean value) {
		return value ? 1 : 0;
	}

	public static boolean isEmpty(Object argString) {
		if ((argString == null)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean trueIfYes(String b) {
		if (b.equals("Y")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isCompletlyEmpty(String argString) {
		if ((argString == null) || (argString.trim().length() == 0) || (argString.equalsIgnoreCase("null"))) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isVaildNumber(Long number) {
		if (null != number && number != 0)
			return true;
		else
			return false;
	}

	public static boolean isVaildNumber(Integer number) {
		if (null != number && number != 0)
			return true;
		else
			return false;
	}

	public static boolean isVaildNumber(String number) {
		if (!isCompletlyEmpty(number) && !"0.00".equalsIgnoreCase(number) && !"0".equalsIgnoreCase(number)
				&& !"0.0".equalsIgnoreCase(number)) {
			try {
				Long.valueOf(number);
			} catch (NumberFormatException nfe) {
				return false;
			}
			return true;
		} else
			return false;
	}
	
	public static Date convertDatePattern(String dateInString) throws Exception {
		Date date = null;
		Timestamp timestamp = null;
		String pattern = "yyyy-MM-dd";
		try {
			if (!isCompletlyEmpty(dateInString)) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
				simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				date = simpleDateFormat.parse(dateInString);
				timestamp = new Timestamp(date.getTime());
			}
		} catch (Exception e) {
			throw e;
		}
		return timestamp;
	}

	public static ModelMapper getModelMapper() {
		if (modelMapper == null) {
			modelMapper = new ModelMapper();
			modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		}
		return modelMapper;
	}

	public static String getDiff(DiffResult<?> diffResult) {
		try {
			if (isEmpty(diffResult))
				return "";
			List<String> list = new ArrayList<>();
			for (Diff<?> diff : diffResult.getDiffs()) {
				if (!CommonUtls.isCompletlyEmpty(diff.getFieldName())) {
					StringBuilder data = new StringBuilder(diff.getFieldName() + " : ");
					if (isEmpty(diff.getLeft()) && !isEmpty(diff.getRight())) {
						data.append(diff.getRight() + Constants.AUDIT_ADD_DELIMITER);
						list.add(data.toString());
					}
					if (!isEmpty(diff.getLeft()) && isEmpty(diff.getRight())) {
						data.append(diff.getLeft() + Constants.AUDIT_REMOVE_DELIMITER);
						list.add(data.toString());
					}
					if (!isEmpty(diff.getLeft()) && !isEmpty(diff.getRight())) {
						data.append(diff.getLeft() + " has changed to " + diff.getRight() + Constants.AUDIT_DELIMITER);
						list.add(data.toString());
					}
				}
			}
			return list.stream().collect(Collectors.joining(""));
		} catch (Exception e) {
		}
		return "";
	}



	public static String convertTime(Date date) {
		String convertedDate = "";
		try {
			if (!isEmpty(date)) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
				TimeZone etTimeZone = TimeZone.getTimeZone("Asia/Kolkata");
				sdf.setTimeZone(etTimeZone);
				convertedDate = sdf.format(date);
			}
		} catch (Exception e) {
			return convertedDate;
		}
		return convertedDate;
	}
	
	public static String getDiffForString(String feild, String oldData, String newData) {
		try {
			if (isCompletlyEmpty(feild))
				return "";
			String data = feild + " : ";
			if (isCompletlyEmpty(oldData) && !isCompletlyEmpty(newData)) {
				data += newData + Constants.AUDIT_ADD_DELIMITER;
			}
			if (!isCompletlyEmpty(oldData) && isCompletlyEmpty(newData)) {
				data += oldData + Constants.AUDIT_REMOVE_DELIMITER;
			}
			if (!isCompletlyEmpty(oldData) && !isCompletlyEmpty(newData)) {
				if (!oldData.equals(newData))
					data += oldData + "<strong> has changed to </strong>" + newData;
			}
			if ((feild + " : ").equals(data))
				return "";
			return data;
		} catch (Exception e) {
			return "";
		}
	}
	
	public static String datetoString(Date date, String dateFormat) {
		String result;
		try {
			result = new SimpleDateFormat(dateFormat).format(date);
		} catch (Exception e) {
			return date.toString();
		}
		return result;
	}

	public static void populateCommonDto(UserPrincipal user, CommonDTO dto) {
		dto.setLoginUserId(user.getId());
		dto.setLoginFullName(user.getUsername());
		dto.setRoleName(user.getRoleName());
	}


	public static String dateFormat(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String requiredDate = null;
		if (date != null) {
			requiredDate = sdf.format(date);
		}
		return requiredDate;
	}

}
