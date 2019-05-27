package ua.com.lineup.enrollmentSystem.service.sms.steps;

import org.springframework.stereotype.Service;
import ua.com.lineup.enrollmentSystem.model.entity.organizationalstructure.City;
import ua.com.lineup.enrollmentSystem.model.entity.organizationalstructure.Province;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.enumeration.StepSms;
import ua.com.lineup.enrollmentSystem.properties.TranslationProperties;
import ua.com.lineup.enrollmentSystem.service.client.OzekiApiCaller;
import ua.com.lineup.enrollmentSystem.service.organizationalstructure.LocationService;
import ua.com.lineup.enrollmentSystem.service.statistic.RegistrationStatisticSmsService;
import ua.com.lineup.enrollmentSystem.service.statistic.SessionStatisticService;
import ua.com.lineup.enrollmentSystem.service.userbiometric.CitizenService;

import java.util.List;

@Service
public class StepChooseProvinceService extends StepChooseGeographicalObjectService {

    public StepChooseProvinceService(RegistrationStatisticSmsService registrationStatisticSmsService,
                                     OzekiApiCaller ozekiApiCaller, CitizenService citizenService,
                                     LocationService locationService, SessionStatisticService sessionStatisticService,
                                     TranslationProperties translationProperties) {
        super(registrationStatisticSmsService, ozekiApiCaller, citizenService, locationService, sessionStatisticService, translationProperties);
        stepBackDisplayMsg = StepSms.CHOOSE_COUNTRY;
        stepBackName = StepSms.CHOOSE_REGION;
        nextStepName = StepSms.CHOOSE_CITY;
        entityName = translationProperties.getPROVINCE();
    }

    public String processSmsRequest() {

        if (!isInteger(smsText)) {
            return errorDetect();
        }
        int answer = Integer.parseInt(smsText);
        List<Province> allProvinces = locationService.getAllProvinceByRegionId(registrationStatisticSms.getRegionId());
        if (answer == allProvinces.size() + 1) {
            // step BACK detected
            return back();
        }
        Province province = getLocationsByUserAnswer(allProvinces, answer);
        if (province == null) {
            return errorDetect();
        }
        registrationStatisticSms.setProvinceId(province.getId());
        return showNextStepSms();
    }


    public String getSmsText() {
        List<City> allCities = locationService.getAllCityByProvinceId(registrationStatisticSms.getProvinceId());
        return displayOptionList(allCities,allSmsStepsService.getStepServiceByName(nextStepName).getEntityName());
    }

}
