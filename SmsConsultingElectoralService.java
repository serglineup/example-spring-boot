package ua.com.lineup.enrollmentSystem.service.sms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.lineup.enrollmentSystem.model.dto.request.SMSRequest;
import ua.com.lineup.enrollmentSystem.model.dto.response.GeographicalStructureResponse;
import ua.com.lineup.enrollmentSystem.model.entity.enrollment.GeographicalDivision;
import ua.com.lineup.enrollmentSystem.model.entity.enrollment.Voter;
import ua.com.lineup.enrollmentSystem.model.entity.enrollment.enumeration.RangeName;
import ua.com.lineup.enrollmentSystem.model.entity.existingvoters.ExistingVoter;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.RegistrationStatisticSms;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.enumeration.StepSms;
import ua.com.lineup.enrollmentSystem.model.entity.userbiometric.Citizen;
import ua.com.lineup.enrollmentSystem.properties.TranslationProperties;
import ua.com.lineup.enrollmentSystem.service.client.OzekiApiCaller;
import ua.com.lineup.enrollmentSystem.service.enrollment.RangeService;
import ua.com.lineup.enrollmentSystem.service.enrollment.VoterService;
import ua.com.lineup.enrollmentSystem.service.existingvoters.ExistingVoterService;
import ua.com.lineup.enrollmentSystem.service.organizationalstructure.LocationService;
import ua.com.lineup.enrollmentSystem.service.statistic.RegistrationStatisticSmsService;
import ua.com.lineup.enrollmentSystem.service.userbiometric.CitizenService;

@Service
public class SmsConsultingElectoralService {

    private final LocationService locationService;

    private final CitizenService citizenService;
    private final ExistingVoterService existingVoterService;
    private final VoterService voterService;
    private final OzekiApiCaller ozekiApiCaller;
    private final RegistrationStatisticSmsService registrationStatisticSmsService;
    private final RangeService rangeService;
    private final TranslationProperties translationProperties;

    private boolean isPassport;

    @Autowired
    public SmsConsultingElectoralService(LocationService locationService,
                                         CitizenService citizenService,
                                         ExistingVoterService existingVoterService,
                                         VoterService voterService,
                                         OzekiApiCaller ozekiApiCaller,
                                         RegistrationStatisticSmsService registrationStatisticSmsService,
                                         RangeService rangeService,
                                         TranslationProperties translationProperties) {
        this.locationService = locationService;
        this.citizenService = citizenService;
        this.existingVoterService = existingVoterService;
        this.voterService = voterService;
        this.ozekiApiCaller = ozekiApiCaller;
        this.registrationStatisticSmsService = registrationStatisticSmsService;
        this.rangeService = rangeService;
        this.translationProperties = translationProperties;
    }

    public String processSmsRequest(String smsText, String senderNumber, RegistrationStatisticSms registrationStatisticSms) {

        String pinOrPassport = parsePinOrPassport(smsText);
        Citizen citizen = findCitizen(pinOrPassport);
        if (citizen == null) {
            String errorText = translationProperties.getPIN_OR_INVALID_PASSPORT_NUMBER_PLEASE_TRY_AGAIN();
            SMSRequest smsRequest = new SMSRequest(senderNumber, errorText);
            ozekiApiCaller.sendMessage(smsRequest);
            registrationStatisticSms.setIsProcessFinished(true);
            registrationStatisticSms.getSteps().add(StepSms.WRONG_SMS_TEXT);
            registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
            return errorText;
        }

        ExistingVoter existingVoter = existingVoterService.findExistingVoter(isPassport, pinOrPassport, pinOrPassport);
        Voter voter = voterService.findVoter(isPassport, pinOrPassport, pinOrPassport);
        String msgText = "";
        if (existingVoter == null && voter == null) {
            msgText = generateNotRegisteredMsg(citizen);
            SMSRequest smsRequest = new SMSRequest(senderNumber, msgText);
            ozekiApiCaller.sendMessage(smsRequest);
            registrationStatisticSms.setIsProcessFinished(true);
            registrationStatisticSms.getSteps().add(StepSms.CONSULTING_DISPLAY_INFO);
            registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
            return msgText;
        } else {
            //Already registered
            if (voter != null) {
                msgText = generateCitizenAlreadyRegisteredMsg(citizen.getIdentificationId(), citizen.getPassport(),
                        citizen.getFirstName1(), citizen.getLastName(), voter.getGeographicalDivision());
            } else {//existingVoter != null
                GeographicalStructureResponse geographicalStructureResponse = locationService.getGeographicalStructureByVotingGUID(existingVoter.getVotingPointGuid());
                GeographicalDivision geographicalDivision = new GeographicalDivision();
                geographicalDivision.setCountry(geographicalStructureResponse.getCountry().getName());
                geographicalDivision.setRegion(geographicalStructureResponse.getRegion().getName());
                geographicalDivision.setProvince(geographicalStructureResponse.getProvince().getName());
                geographicalDivision.setCity(geographicalStructureResponse.getCommune().getName());
                geographicalDivision.setDistrict(geographicalStructureResponse.getArrondissement().getName());
                geographicalDivision.setVillage(geographicalStructureResponse.getSecteurvillage().getName());
                geographicalDivision.setPlace(geographicalStructureResponse.getLieu().getName());
                geographicalDivision.setSpecificLocation(geographicalStructureResponse.getEmplacement().getName());
                geographicalDivision.setVotingPoint(geographicalStructureResponse.getBureauvote().getName());
                msgText = generateCitizenAlreadyRegisteredMsg(citizen.getIdentificationId(), citizen.getPassport(),
                        citizen.getFirstName1(), citizen.getLastName(), geographicalDivision);
            }
        }
        SMSRequest smsRequest = new SMSRequest(senderNumber, msgText);
        ozekiApiCaller.sendMessage(smsRequest);
        registrationStatisticSms.setIsProcessFinished(true);
        registrationStatisticSms.getSteps().add(StepSms.CONSULTING_DISPLAY_INFO);
        registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
        return msgText;
    }

    private String generateCitizenAlreadyRegisteredMsg(String identificationId, String passportNumber,
                                                       String firstName, String lastName,
                                                       GeographicalDivision geographicalDivision) {
        return translationProperties.getALREADY_REGISTERED() + ": "
                + identificationId + "/"
                + passportNumber + "/"
                + firstName + " "
                + lastName + "/"
                + geographicalDivision.getCountry() + "/"
                + geographicalDivision.getRegion() + "/"
                + geographicalDivision.getProvince() + "/"
                + geographicalDivision.getCity() + "/"
                + geographicalDivision.getVillage() + "/"
                + geographicalDivision.getPlace() + "/"
                + geographicalDivision.getSpecificLocation() + "/"
                + translationProperties.getVOTING_POINT() + ":" + geographicalDivision.getVotingPoint();
    }

    private String generateNotRegisteredMsg(Citizen citizen) {
        String msg = "";
        if (isPassport) {
            msg += translationProperties.getPASSPORT_NUMBER() + "/" + citizen.getPassport();
        } else {
            msg += translationProperties.getPIN_NUMBER_CNIB() + "/" + citizen.getIdentificationId();
        }
        msg += " " + translationProperties.getIS_NOT_REGISTERED_ON_THE_LIST_OF_ELECTORS() + " ";

        if (rangeService.isRangeNameActive(RangeName.CENSUS)) {
            msg += translationProperties.getREGISTRATION_SMS_I_PIN_AT_12345678();
        }
        return msg;
    }

    private Citizen findCitizen(String pinOrPassport) {
        isPassport = true;
        Citizen citizen = citizenService.findCitizen(true, pinOrPassport, pinOrPassport);
        if (citizen == null) {
            isPassport = false;
            citizen = citizenService.findCitizen(false, pinOrPassport, pinOrPassport);
        }
        return citizen;
    }

    private String parsePinOrPassport(String smsText) {
        smsText = smsText.replaceAll("\\s+", "");
        smsText = smsText.toUpperCase();
        return smsText.substring(1).trim();
    }

}
