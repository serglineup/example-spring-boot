package ua.com.lineup.enrollmentSystem.service.sms.steps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.lineup.enrollmentSystem.model.dto.request.SMSRequest;
import ua.com.lineup.enrollmentSystem.model.dto.response.GeographicalStructureResponse;
import ua.com.lineup.enrollmentSystem.model.entity.enrollment.GeographicalDivision;
import ua.com.lineup.enrollmentSystem.model.entity.enrollment.Voter;
import ua.com.lineup.enrollmentSystem.model.entity.existingvoters.ExistingVoter;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.SessionStatistic;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.enumeration.StepSms;
import ua.com.lineup.enrollmentSystem.model.entity.userbiometric.Citizen;
import ua.com.lineup.enrollmentSystem.properties.TranslationProperties;
import ua.com.lineup.enrollmentSystem.service.client.OzekiApiCaller;
import ua.com.lineup.enrollmentSystem.service.enrollment.VoterService;
import ua.com.lineup.enrollmentSystem.service.existingvoters.ExistingVoterService;
import ua.com.lineup.enrollmentSystem.service.organizationalstructure.LocationService;
import ua.com.lineup.enrollmentSystem.service.statistic.RegistrationStatisticSmsService;
import ua.com.lineup.enrollmentSystem.service.statistic.SessionStatisticService;
import ua.com.lineup.enrollmentSystem.service.userbiometric.CitizenService;

@Service
public class StepProcessCnibPassportVerificationService extends StepChooseGeographicalObjectService {

    private final OzekiApiCaller ozekiApiCaller;
    private final RegistrationStatisticSmsService registrationStatisticSmsService;
    private final CitizenService citizenService;
    private final ExistingVoterService existingVoterService;
    private final VoterService voterService;
    private final LocationService locationService;
    private final SessionStatisticService sessionStatisticService;

    private boolean isPassport;
    private Citizen citizen;
    private GeographicalStructureResponse geographicalStructureResponse;

    @Autowired
    public StepProcessCnibPassportVerificationService(
            RegistrationStatisticSmsService registrationStatisticSmsService,
            OzekiApiCaller ozekiApiCaller,
            CitizenService citizenService,
            ExistingVoterService existingVoterService,
            VoterService voterService,
            LocationService locationService, SessionStatisticService sessionStatisticService, TranslationProperties translationProperties) {

        this.registrationStatisticSmsService = registrationStatisticSmsService;
        this.ozekiApiCaller = ozekiApiCaller;
        this.citizenService = citizenService;
        this.existingVoterService = existingVoterService;
        this.voterService = voterService;
        this.locationService = locationService;
        this.sessionStatisticService = sessionStatisticService;
        this.translationProperties = translationProperties;
    }

    public String processSmsRequest() {

        String pinOrPassport = parsePinOrPassport(smsText);
        citizen = findCitizen(pinOrPassport);
        if (citizen == null) {
            return errorDetect();
        }
        registrationStatisticSms.setPassport(citizen.getPassport());
        registrationStatisticSms.setIdentificationId(citizen.getIdentificationId());
        registrationStatisticSms.setFirstName(citizen.getFirstName1());
        registrationStatisticSms.setLastName(citizen.getLastName());
        registrationStatisticSms.setIsPassport(isPassport);
        SessionStatistic sessionStatistic = sessionStatisticService.findSession(registrationStatisticSms.getSessionStatisticId());

        ExistingVoter existingVoter = existingVoterService.findExistingVoter(isPassport, pinOrPassport, pinOrPassport);
        Voter voter = voterService.findVoter(isPassport, pinOrPassport, pinOrPassport);
        String msgText = "";
        if (existingVoter == null && voter == null) {
            geographicalStructureResponse = locationService.getGeographicalStructureByVillageId(citizen.getIdSecteurVillage());
            registrationStatisticSms.setCountryCode(geographicalStructureResponse.getCountry().getCode());
            registrationStatisticSms.setRegionId(geographicalStructureResponse.getRegion().getId());
            registrationStatisticSms.setProvinceId(geographicalStructureResponse.getProvince().getId());
            registrationStatisticSms.setCityId(geographicalStructureResponse.getCommune().getId());
            registrationStatisticSms.setDistrictId(geographicalStructureResponse.getArrondissement().getId());
            registrationStatisticSms.setVillageId(geographicalStructureResponse.getSecteurvillage().getId());
            registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);

            sessionStatistic.setCountryCode(geographicalStructureResponse.getCountry().getCode());
            sessionStatistic.setRegionId(geographicalStructureResponse.getRegion().getId());
            sessionStatistic.setProvinceId(geographicalStructureResponse.getProvince().getId());
            sessionStatistic.setCityId(geographicalStructureResponse.getCommune().getId());
            sessionStatistic.setDistrictId(geographicalStructureResponse.getArrondissement().getId());
            sessionStatistic.setVillageId(geographicalStructureResponse.getSecteurvillage().getId());
            sessionStatisticService.updateSession(sessionStatistic);

            msgText = getSmsText();
            SMSRequest smsRequest = new SMSRequest(senderNumber, msgText);
            ozekiApiCaller.sendMessage(smsRequest);
            registrationStatisticSms.getSteps().add(StepSms.IDENTIFICATION_SUMMARY);
            registrationStatisticSms.setWrongAttempts(0);
            registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
            return msgText;
        } else {
            sessionStatistic.setIsExistingVoter(true);
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
            sessionStatisticService.updateSession(sessionStatistic);
        }
        SMSRequest smsRequest = new SMSRequest(senderNumber, msgText);
        ozekiApiCaller.sendMessage(smsRequest);
        registrationStatisticSms.setIsProcessFinished(true);
        registrationStatisticSms.getSteps().add(StepSms.CONSULTING_DISPLAY_INFO);
        registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
        return msgText;
    }

    @Override
    public String errorDetect() {
        String errorText = translationProperties.getPIN_OR_INVALID_PASSPORT_NUMBER_PLEASE_TRY_AGAIN();
        SMSRequest smsRequest = new SMSRequest(senderNumber, errorText);
        ozekiApiCaller.sendMessage(smsRequest);
        registrationStatisticSms.setIsProcessFinished(true);
        registrationStatisticSms.getSteps().add(StepSms.WRONG_SMS_TEXT);
        registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
        return errorText;
    }

    @Override
    public String back() {
        return null;
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


    public String getSmsText() {
        if (citizen == null) {
            citizen = citizenService.findCitizen(true, registrationStatisticSms.getPassport(), "");
        }
        if (geographicalStructureResponse == null) {
            geographicalStructureResponse = locationService.getGeographicalStructureByVillageId(citizen.getIdSecteurVillage());
        }

        return translationProperties.getIDENTIFICATION_SUMMARY() + " "
                + translationProperties.getPIN_CNIB_NUMPASS() + " "
                + citizen.getIdentificationId() + " / " + citizen.getPassport()
                + " " + translationProperties.getCORRESPONDING_TO()
                + " / " + citizen.getFirstName1() + " " + citizen.getLastName() + ". "
                + translationProperties.getACCORDING_TO_YOUR_DATA_YOU_WILL_BE_REGISTERED_IN()
                + geographicalStructureResponse.getCountry().getName() + "/"
                + geographicalStructureResponse.getRegion().getName() + "/"
                + geographicalStructureResponse.getProvince().getName() + "/"
                + geographicalStructureResponse.getCommune().getName() + "/"
                + geographicalStructureResponse.getArrondissement().getName() + "/"
                + geographicalStructureResponse.getSecteurvillage().getName()
                + ". " + translationProperties.getKEEP_THESE_DATA_OR_CHANGE_THEM();
    }

    private String parsePinOrPassport(String smsText) {
        smsText = smsText.replaceAll("\\s+", "");
        smsText = smsText.toUpperCase();
        return smsText.substring(1).trim();
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

}
