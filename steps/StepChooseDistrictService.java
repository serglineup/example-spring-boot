package ua.com.lineup.enrollmentSystem.service.sms.steps;

import org.springframework.stereotype.Service;
import ua.com.lineup.enrollmentSystem.model.entity.organizationalstructure.District;
import ua.com.lineup.enrollmentSystem.model.entity.organizationalstructure.Village;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.enumeration.StepSms;
import ua.com.lineup.enrollmentSystem.properties.TranslationProperties;
import ua.com.lineup.enrollmentSystem.service.client.OzekiApiCaller;
import ua.com.lineup.enrollmentSystem.service.organizationalstructure.LocationService;
import ua.com.lineup.enrollmentSystem.service.statistic.RegistrationStatisticSmsService;
import ua.com.lineup.enrollmentSystem.service.statistic.SessionStatisticService;
import ua.com.lineup.enrollmentSystem.service.userbiometric.CitizenService;

import java.util.List;

@Service
public class StepChooseDistrictService extends StepChooseGeographicalObjectService {

    public StepChooseDistrictService(RegistrationStatisticSmsService registrationStatisticSmsService,
                                     OzekiApiCaller ozekiApiCaller, CitizenService citizenService,
                                     LocationService locationService, SessionStatisticService sessionStatisticService, TranslationProperties translationProperties) {
        super(registrationStatisticSmsService, ozekiApiCaller, citizenService, locationService, sessionStatisticService, translationProperties);
        stepBackDisplayMsg = StepSms.CHOOSE_PROVINCE;
        stepBackName = StepSms.CHOOSE_CITY;
        nextStepName = StepSms.CHOOSE_SECTOR_OR_VILLAGE;
        entityName = translationProperties.getDISTRICT();
    }

    public String processSmsRequest() {

        if (!isInteger(smsText)) {
            return errorDetect();
        }
        int answer = Integer.parseInt(smsText);
        List<District> allDistricts = locationService.getAllDistrictByCityId(registrationStatisticSms.getCityId());
        if (answer == allDistricts.size() + 1) {
            // step BACK detected
            return back();
        }
        District district = getLocationsByUserAnswer(allDistricts, answer);
        if (district == null) {
            return errorDetect();
        }
        registrationStatisticSms.setDistrictId(district.getId());
        return showNextStepSms();
    }


    public String getSmsText() {
        List<Village> allVillages = locationService.getAllVillageByDistrictId(registrationStatisticSms.getDistrictId());
        return displayOptionList(allVillages,allSmsStepsService.getStepServiceByName(nextStepName).getEntityName());
    }

}
