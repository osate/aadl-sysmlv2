standard library package AADL_Project {
	attribute def Time :> ISQ::DurationValue {
		attribute :>> num: ScalarValues::Integer;
	}
	
	attribute def Time_Range {
		attribute minimum : Time;
		attribute maximum : Time;
		attribute delta : Time;
	}
	
    package Time_Units {
		attribute <ps> picosecond : SI::DurationUnit {
			:>> unitConversion: MeasurementReferences::ConversionByPrefix {
				:>> prefix = SI::pico;
				:>> referenceUnit = SI::s;
			}
		}
		
		attribute <ns> nanosecond : SI::DurationUnit {
			:>> unitConversion: MeasurementReferences::ConversionByPrefix {
				:>> prefix = SI::nano;
				:>> referenceUnit = SI::s;
			}
		}
		
		attribute <us> microsecond : SI::DurationUnit {
			:>> unitConversion: MeasurementReferences::ConversionByPrefix {
				:>> prefix = SI::micro;
				:>> referenceUnit = SI::s;
			}
		}
		
		attribute <ms> millisecond : SI::DurationUnit {
			:>> unitConversion: MeasurementReferences::ConversionByPrefix {
				:>> prefix = SI::milli;
				:>> referenceUnit = SI::s;
			}
		}
	}
}