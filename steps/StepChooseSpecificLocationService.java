package ua.com.lineup.enrollmentSystem.service.sms.steps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.lineup.enrollmentSystem.model.entity.enrollment.GeographicalDivision;
import ua.com.lineup.enrollmentSystem.model.entity.organizationalstructure.SpecificLocation;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.SessionStatistic;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.enumeration.StepSms;
import ua.com.lineup.enrollmentSystem.model.entity.userbiometric.Citizen;
import ua.com.lineup.enrollmentSystem.properties.TranslationProperties;
import ua.com.lineup.enrollmentSystem.service.client.OzekiApiCaller;
import ua.com.lineup.enrollmentSystem.service.enrollment.VoterService;
import ua.com.lineup.enrollmentSystem.service.organizationalstructure.LocationService;
import ua.com.lineup.enrollmentSystem.service.statistic.RegistrationStatisticSmsService;
import ua.com.lineup.enrollmentSystem.service.statistic.SessionStatisticService;
import ua.com.lineup.enrollmentSystem.service.userbiometric.CitizenService;

import java.util.List;

@Service
public class StepChooseSpecificLocationService extends StepChooseGeographicalObjectService {

    private VoterService voterService;

    public StepChooseSpecificLocationService(RegistrationStatisticSmsService registrationStatisticSmsService, OzekiApiCaller ozekiApiCaller,
                                             CitizenService citizenService, LocationService locationService,
                                             SessionStatisticService sessionStatisticService, TranslationProperties translationProperties) {
        super(registrationStatisticSmsService, ozekiApiCaller, citizenService, locationService, sessionStatisticService, translationProperties);
        stepBackDisplayMsg = StepSms.IDENTIFICATION_SUMMARY;
        stepBackName = StepSms.CHOOSE_PLACE;
        nextStepName = StepSms.CONFIRM_REGISTRATION;
        entityName = translationProperties.getSPECIFIC_LOCATION();
    }

    public String processSmsRequest() {

        if (!isInteger(smsText)) {
            return errorDetect();
        }
        int answer = Integer.parseInt(smsText);
        List<SpecificLocation> allSpecificLocations = locationService.getAllLocationByPlaceId(registrationStatisticSms.getPlaceId());
        if (answer == allSpecificLocations.size() + 1) {
            // step BACK detected
            return back();
        }

        SpecificLocation specificLocation = getLocationsByUserAnswer(allSpecificLocations, answer);
        if (specificLocation == null) {
            return errorDetect();
        }
        SessionStatistic sessionStatistic = sessionStatisticService.findSession(registrationStatisticSms.getSessionStatisticId());
        sessionStatistic.setSpecificLocationId(specificLocation.getId());
        sessionStatisticService.updateSession(sessionStatistic);
        registrationStatisticSms.setSpecificLocationId(specificLocation.getId());
        return showNextStepSms();
    }


    @Autowired
    public void setVoterService(VoterService voterService) {
        this.voterService = voterService;
    }

    public String getSmsText() {
        Citizen citizen = citizenService.findCitizen(true, registrationStatisticSms.getPassport(), "");
        GeographicalDivision geographicalDivision = voterService.getVotingPointBySpecificLocationId(registrationStatisticSms.getSpecificLocationId());
        String msg = translationProperties.getREGISTRATION_SUMMARY() + " NIP-CNIB / NUMPASS: " + registrationStatisticSms.getIdentificationId() + " / "
                + registrationStatisticSms.getPassport() + " / "
                + citizen.getFirstName1() + " " + citizen.getLastName() + " / "
                + geographicalDivision.getCity() + " / "
                + geographicalDivision.getDistrict() + "/ "
                + geographicalDivision.getVillage() + "/ "
                + geographicalDivision.getPlace() + "/ "
                + geographicalDivision.getSpecificLocation() + "/ "
                + geographicalDivision.getVotingPoint();

        msg += " " + translationProperties.getPLEASE_CONFIRM_REGISTRATION_CONFIRM_1_CANCEL_2__GO_BACK_3();
        return msg;
    }

}
