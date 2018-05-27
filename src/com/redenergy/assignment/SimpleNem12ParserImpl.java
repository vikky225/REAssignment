package com.redenergy.assignment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.redenergy.assignment.Quality.valueOf;

import static java.util.Objects.isNull;

/**
 * Nem12 parser implementation which reads nem12 file and returns the collection
 * of meterreads.
 */
public class SimpleNem12ParserImpl implements SimpleNem12Parser {
	private static final String COMMA = ",";
	private static int linenumber;
	private static List<MeterRead> meterReads = new ArrayList<>();

	/**
	 * Parses Simple NEM12 file.
	 *
	 * @param simpleNem12File
	 *            file in Simple NEM12 format
	 * @return Collection of <code>MeterRead</code> that represents the data in
	 *         the given file.
	 */
	@Override
	public Collection<MeterRead> parseSimpleNem12(File simpleNem12File) {
		try {

			InputStream inputFS = new FileInputStream(simpleNem12File);
			BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));
			meterReads = br.lines().map(mapToItem).collect(Collectors.toList());

			// This will validate if meter reads available and it will removve
			// null from the list so it will contain only meterreads
			try {
				validateMeterRecordsAvailable(meterReads);
			} catch (Exception e) {

				e.getMessage();
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return meterReads;
	}

	/**
	 * validation of csv file if it does have records and remove null entry and
	 * keep only valid meter records
	 *
	 * @param inputList
	 * @throws Exception
	 * @throws SimpleNemParserException
	 */
	private void validateMeterRecordsAvailable(List<MeterRead> inputList) throws Exception {

		if (isNull(inputList) || inputList.isEmpty()) {
			throw new Exception("there is no contain in file i.e. there is no meter records");
		}

		// removing null from the list so it will contain only meter records
		inputList.removeIf(Objects::isNull);
	}

	private Function<String, MeterRead> mapToItem = (line) -> {
		String[] records = line.split(COMMA);// a CSV has comma separated lines

		String firstColumn = records[0].trim();
		linenumber++;

		CheckFirstRecordIs100(linenumber, firstColumn);

		if ("200".equals(firstColumn)) {
			MeterRead meterRead = createNewMeterRecord(records);
			meterReads.add(meterRead);
			return meterRead;
		}

		if ("300".equals(firstColumn)) {
			// Get the last element from meterreads list and add the meter
			// volume
			MeterRead meterRead = meterReads.get(meterReads.size() - 1);

			if (QualityValidationCheck(records)) {
				MeterVolume meterVolume = new MeterVolume(BigDecimal.valueOf(Double.parseDouble(records[2])),
						valueOf(records[3]));
				meterRead.appendVolume(parseDate(records[1]), meterVolume);

			}

		}

		if ("900".equals(firstColumn)) {
			// if last line equals to 900 than return null
		}

		return null;

	};

	private boolean QualityValidationCheck(String[] records) {

		if (isNull(records[3])
				|| (Quality.valueOf(records[3]) != Quality.A) && (Quality.valueOf(records[3]) != Quality.E)) {

			try {
				throw new Exception("Quality Validation Fail, should not be null and shoulld be A or E");
			} catch (Exception e) {
				e.getMessage();
			}

		} else {

			return true;
		} 
		return false;
	}

	/**
	 * Create New Record for Meter Read
	 *
	 * @param record
	 * @return -meter read
	 * 
	 */
	private MeterRead createNewMeterRecord(String[] record) {
		MeterRead meterRead = new MeterRead();
		if (isNull(record[1]) || record[1].length() != 10) {

			try {
				throw new Exception("NMI Validation fail,Nmi Should not be null and should be of length 10");
			} catch (Exception e) {

				e.getMessage();
			}
		} else {
			meterRead.setNmi(record[1]);
		}

		if (isNull(record[2]) || EnergyUnit.valueOf(record[2]) != EnergyUnit.KWH) {
			try {
				throw new Exception("Energy Unit Validation Fail , EU should not be null and should be KWH");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.getMessage();
			}
		} else {
			meterRead.setEnergyUnit(EnergyUnit.valueOf(record[2]));
		}

		return meterRead;

	}

	private void CheckFirstRecordIs100(int linenumber, String firstColumn) {

		// if line number is 1 check first column is 100 or else throw exception
		if (linenumber == 1) {
			if (!("100".equals(firstColumn))) {
				try {
					throw new Exception("first columne first line is not starting with 100");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.getMessage();
				}

			}
		}
	}

	/**
	 * Parsing the date
	 *
	 * @param date
	 * 
	 * @return - LocalDate object
	 * 
	 */
	private LocalDate parseDate(String date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		formatter = formatter.withLocale(Locale.ENGLISH);
		return LocalDate.parse(date, formatter);
	}

}
