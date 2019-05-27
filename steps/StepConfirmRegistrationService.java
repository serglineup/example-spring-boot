package ua.com.lineup.enrollmentSystem.service.sms.steps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.lineup.enrollmentSystem.converter.RegistrationStatisticConverter;
import ua.com.lineup.enrollmentSystem.model.dto.request.RegistrateVoterRequest;
import ua.com.lineup.enrollmentSystem.model.dto.request.SMSRequest;
import ua.com.lineup.enrollmentSystem.model.entity.enrollment.GeographicalDivision;
import ua.com.lineup.enrollmentSystem.model.entity.enrollment.Voter;
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

@Service
public class StepConfirmRegistrationService extends StepChooseGeographicalObjectService {

    private RegistrationStatisticConverter registrationStatisticConverter;
    private VoterService voterService;


    public StepConfirmRegistrationService(RegistrationStatisticSmsService registrationStatisticSmsService,
                                          OzekiApiCaller ozekiApiCaller, CitizenService citizenService,
                                          LocationService locationService, SessionStatisticService sessionStatisticService,
                                          TranslationProperties translationProperties) {
        super(registrationStatisticSmsService, ozekiApiCaller, citizenService, locationService, sessionStatisticService, translationProperties);
        stepBackDisplayMsg = StepSms.CHOOSE_PLACE;
        stepBackName = StepSms.CHOOSE_SPECIFIC_LOCATION;
        nextStepName = StepSms.DISPLAY_REGISTRATION_INFO;
    }

    @Autowired
    public void setRegistrationStatisticConverter(RegistrationStatisticConverter registrationStatisticConverter) {
        this.registrationStatisticConverter = registrationStatisticConverter;
    }

    @Autowired
    public void setVoterService(VoterService voterService) {
        this.voterService = voterService;
    }

    public String processSmsRequest() {

        String msgText;
        SMSRequest smsRequest;
        if (smsText.equals("1")) {
            msgText = getSmsText();
            smsRequest = new SMSRequest(senderNumber, msgText);
            ozekiApiCaller.sendMessage(smsRequest);
            registrationStatisticSms.getSteps().add(StepSms.DISPLAY_REGISTRATION_INFO);
            registrationStatisticSms.setIsProcessFinished(true);
            registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);

            RegistrateVoterRequest registrateVoterRequest = registrationStatisticConverter
                    .convertingRegistrationStatisticSmsToRegistrateVoterRequest(registrationStatisticSms);
            SessionStatistic sessionStatistic = sessionStatisticService.findSession(registrationStatisticSms.getSessionStatisticId());
            registrateVoterRequest.setIsExistingVoter(sessionStatistic.getIsExistingVoter());
            registrateVoterRequest.setLocationId(sessionStatistic.getSpecificLocationId());
            registrateVoterRequest.setChannel(sessionStatistic.getChannel());
            Voter voter = voterService.createVoter(registrateVoterRequest);
            sessionStatistic.setVoterId(voter.getId());
            sessionStatistic.setIsAlreadyRegistered(true);
            sessionStatisticService.updateSession(sessionStatistic);

            return msgText;

        } else if (smsText.equals("2")) {
            msgText = generateNonRegisteredMsg();
            smsRequest = new SMSRequest(senderNumber, msgText);
            ozekiApiCaller.sendMessage(smsRequest);
            registrationStatisticSms.getSteps().add(StepSms.DISPLAY_REGISTRATION_INFO);
            registrationStatisticSms.setIsProcessFinished(true);
            registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
            return msgText;
        } else if (smsText.equals("3")) {
            // step BACK detected
            return back();

        }

        return errorDetect();
    }

    private String generateNonRegisteredMsg() {
        return translationProperties.getYOUR_REGISTRATION_OPERATION_IS_CANCELED_PLEASE_RENEW_THE_OPERATION();
    }

    public String getSmsText() {
        Citizen citizen = citizenService.findCitizen(true, registrationStatisticSms.getPassport(), "");
        GeographicalDivision geographicalDivision = voterService.getVotingPointBySpecificLocationId(registrationStatisticSms.getSpecificLocationId());
        registrationStatisticSms.setVotingPointId(geographicalDivision.getVotingPoint());

        return translationProperties.getTHANK_YOU_FOR_COMPLETING_YOUR_ENROLLMENT_WITH_THE_CENI() + " "
                + registrationStatisticSms.getIdentificationId() + "/"
                + registrationStatisticSms.getPassport() + "/"
                + citizen.getFirstName1() + " " + citizen.getLastName() + "/"
                + geographicalDivision.getCountry() + "/"
                + geographicalDivision.getRegion() + "/"
                + geographicalDivision.getProvince() + "/"
                + geographicalDivision.getCity() + "/"
                + geographicalDivision.getVillage() + "/"
                + geographicalDivision.getPlace() + "/"
                + geographicalDivision.getSpecificLocation() + "/"
                + geographicalDivision.getVotingPoint();
    }
}
