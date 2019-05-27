package ua.com.lineup.enrollmentSystem.service.sms.steps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.enumeration.StepSms;

@Service
public class AllSmsStepsService {

    private StepProcessCnibPassportVerificationService stepProcessCnibPassportVerificationService;
    private StepIdentificationSummaryService stepIdentificationSummaryService;
    private StepChoosePlaceService stepChoosePlaceService;
    private StepConfirmRegistrationService stepConfirmRegistrationService;
    private StepChooseSpecificLocationService stepChooseSpecificLocationService;
    private StepChooseCountryService stepChooseCountryService;
    private StepChooseProvinceService stepChooseProvinceService;
    private StepChooseRegionService stepChooseRegionService;
    private StepChooseCityService stepChooseCityService;
    private StepChooseDistrictService stepChooseDistrictService;
    private StepChooseVillageService stepChooseVillageService;


    @Autowired
    public void setStepProcessCnibPassportVerificationService(StepProcessCnibPassportVerificationService stepProcessCnibPassportVerificationService) {
        this.stepProcessCnibPassportVerificationService = stepProcessCnibPassportVerificationService;
    }

    @Autowired
    public void setStepIdentificationSummaryService(StepIdentificationSummaryService stepIdentificationSummaryService) {
        this.stepIdentificationSummaryService = stepIdentificationSummaryService;
    }

    @Autowired
    public void setStepChoosePlaceService(StepChoosePlaceService stepChoosePlaceService) {
        this.stepChoosePlaceService = stepChoosePlaceService;
    }

    @Autowired
    public void setStepConfirmRegistrationService(StepConfirmRegistrationService stepConfirmRegistrationService) {
        this.stepConfirmRegistrationService = stepConfirmRegistrationService;
    }

    @Autowired
    public void setStepConfirmRegistrationService(StepChooseSpecificLocationService stepChooseSpecificLocationService) {
        this.stepChooseSpecificLocationService = stepChooseSpecificLocationService;
    }

    @Autowired
    public void setStepChooseCountryService(StepChooseCountryService stepChooseCountryService) {
        this.stepChooseCountryService = stepChooseCountryService;
    }

    @Autowired
    public void setStepChooseRegionService(StepChooseRegionService stepChooseRegionService) {
        this.stepChooseRegionService = stepChooseRegionService;
    }

    @Autowired
    public void setStepStepChooseProvinceService(StepChooseProvinceService stepChooseProvinceService) {
        this.stepChooseProvinceService = stepChooseProvinceService;
    }

    @Autowired
    public void setStepChooseCityService(StepChooseCityService stepChooseCityService) {
        this.stepChooseCityService = stepChooseCityService;
    }

    @Autowired
    public void setStepChooseDistrictService(StepChooseDistrictService stepChooseDistrictService) {
        this.stepChooseDistrictService = stepChooseDistrictService;
    }

    @Autowired
    public void setStepStepChooseProvinceService(StepChooseVillageService stepChooseVillageService) {
        this.stepChooseVillageService = stepChooseVillageService;
    }

    public StepChooseGeographicalObjectService getStepServiceByName(StepSms stepSmsName) {

        if (StepSms.PROCESS_CNIB_PASSPORT_VERIFICATION == stepSmsName) {
            return stepProcessCnibPassportVerificationService;
        } else if (StepSms.IDENTIFICATION_SUMMARY == stepSmsName) {
            return stepIdentificationSummaryService;
        } else if (StepSms.CHOOSE_PLACE == stepSmsName) {
            return stepChoosePlaceService;
        } else if (StepSms.CHOOSE_SPECIFIC_LOCATION == stepSmsName) {
            return stepChooseSpecificLocationService;
        } else if (StepSms.CONFIRM_REGISTRATION == stepSmsName) {
            return stepConfirmRegistrationService;
        }else if (StepSms.CHOOSE_COUNTRY == stepSmsName) {
            return stepChooseCountryService;
        } else if (StepSms.CHOOSE_REGION == stepSmsName) {
            return stepChooseRegionService;
        } else if (StepSms.CHOOSE_PROVINCE == stepSmsName) {
            return stepChooseProvinceService;
        }else if (StepSms.CHOOSE_CITY == stepSmsName) {
            return stepChooseCityService;
        } else if (StepSms.CHOOSE_DISTRICT == stepSmsName) {
            return stepChooseDistrictService;
        } else if (StepSms.CHOOSE_SECTOR_OR_VILLAGE == stepSmsName) {
            return stepChooseVillageService;
        }
        return null;
    }

}
