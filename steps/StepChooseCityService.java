package ua.com.lineup.enrollmentSystem.service.sms.steps;

import org.springframework.stereotype.Service;
import ua.com.lineup.enrollmentSystem.model.entity.organizationalstructure.City;
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
public class StepChooseCityService extends StepChooseGeographicalObjectService {

    public StepChooseCityService(RegistrationStatisticSmsService registrationStatisticSmsService,
                                 OzekiApiCaller ozekiApiCaller, CitizenService citizenService,
                                 LocationService locationService, SessionStatisticService sessionStatisticService,
                                 TranslationProperties translationProperties) {
        super(registrationStatisticSmsService, ozekiApiCaller, citizenService, locationService, sessionStatisticService, translationProperties);
        stepBackDisplayMsg = StepSms.CHOOSE_REGION;
        stepBackName = StepSms.CHOOSE_PROVINCE;
        entityName = translationProperties.getCITY();
    }

    public String processSmsRequest() {

        if (!isInteger(smsText)) {
            return errorDetect();
        }
        int answer = Integer.parseInt(smsText);
        List<City> allCities = locationService.getAllCityByProvinceId(registrationStatisticSms.getProvinceId());
        if (answer == allCities.size() + 1) {
            // step BACK detected
            return back();
        }
        City city = getLocationsByUserAnswer(allCities, answer);
        if (city == null) {
            return errorDetect();
        }
        registrationStatisticSms.setCityId(city.getId());
        return showNextStepSms();
    }


    public String getSmsText() {

        if (CITY_IDS_WITH_DISTRICTS.contains(registrationStatisticSms.getCityId())) {
            nextStepName = StepSms.CHOOSE_DISTRICT;
            List<District> allDistricts = locationService.getAllDistrictByCityId(registrationStatisticSms.getCityId());
            return displayOptionList(allDistricts, allSmsStepsService.getStepServiceByName(nextStepName).getEntityName());
        }

        // auto assign district it should be only one
        List<District> allDistricts = locationService.getAllDistrictByCityId(registrationStatisticSms.getCityId());
        Long districtId = allDistricts.get(0).getId();
        registrationStatisticSms.setDistrictId(districtId);
        registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);

        nextStepName = StepSms.CHOOSE_SECTOR_OR_VILLAGE;
        List<Village> allVillages = locationService.getAllVillageByDistrictId(registrationStatisticSms.getDistrictId());
        return displayOptionList(allVillages, allSmsStepsService.getStepServiceByName(nextStepName).getEntityName());
    }

}
