package ua.com.lineup.enrollmentSystem.service.sms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.lineup.enrollmentSystem.model.dto.request.SMSRequest;
import ua.com.lineup.enrollmentSystem.model.dto.request.SmsOzekiMessageRequest;
import ua.com.lineup.enrollmentSystem.model.entity.enrollment.enumeration.RangeName;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.RegistrationStatisticSms;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.enumeration.SmsFlow;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.enumeration.StepSms;
import ua.com.lineup.enrollmentSystem.properties.TranslationProperties;
import ua.com.lineup.enrollmentSystem.service.client.OzekiApiCaller;
import ua.com.lineup.enrollmentSystem.service.enrollment.RangeService;
import ua.com.lineup.enrollmentSystem.service.statistic.RegistrationStatisticSmsService;

import javax.servlet.http.HttpServletRequest;

@Service
public class SmsChannelService {

    private final OzekiApiCaller ozekiApiCaller;
    private final SmsConsultingElectoralService smsConsultingElectoralService;
    private final SmsEnrolmentService smsEnrolmentService;
    private final RegistrationStatisticSmsService registrationStatisticSmsService;
    private final RangeService rangeService;
    private final TranslationProperties translationProperties;

    @Autowired
    public SmsChannelService(SmsConsultingElectoralService smsConsultingElectoralService,
                             SmsEnrolmentService smsEnrolmentService,
                             RegistrationStatisticSmsService registrationStatisticSmsService,
                             OzekiApiCaller ozekiApiCaller,
                             RangeService rangeService,
                             TranslationProperties translationProperties) {
        this.smsConsultingElectoralService = smsConsultingElectoralService;
        this.smsEnrolmentService = smsEnrolmentService;
        this.registrationStatisticSmsService = registrationStatisticSmsService;
        this.ozekiApiCaller = ozekiApiCaller;
        this.rangeService = rangeService;
        this.translationProperties = translationProperties;
    }

    public String processSmsRequest(String smsText, String senderNumber, HttpServletRequest httpServletRequest, SmsOzekiMessageRequest smsOzekiMessageRequest) {

        smsText = smsText.trim();
        RegistrationStatisticSms registrationStatisticSms = registrationStatisticSmsService.findRegistrationStatisticSms(senderNumber);
        if (registrationStatisticSms == null) {
            registrationStatisticSms = registrationStatisticSmsService.createRegistrationStatisticSmsStepFirst(senderNumber, smsText, httpServletRequest,smsOzekiMessageRequest);
        } else {
            if (initNewSessionDetect(smsText)) {
                registrationStatisticSmsService.closeAllUnfinishedSession(senderNumber);
                registrationStatisticSms = registrationStatisticSmsService.createRegistrationStatisticSmsStepFirst(senderNumber, smsText, httpServletRequest,smsOzekiMessageRequest);
            }
        }

        if (registrationStatisticSms.getFlow() == SmsFlow.CONSULTING) {

            if (!rangeService.isRangeNameActive(RangeName.CONSULTATION)) {
                String errorText = translationProperties.getTHIS_IS_NOT_CONSULTATION_PHASE();
                SMSRequest smsRequest = new SMSRequest(senderNumber, errorText);
                ozekiApiCaller.sendMessage(smsRequest);
                registrationStatisticSms.setIsProcessFinished(true);
                registrationStatisticSms.getSteps().add(StepSms.PHASE_ERROR);
                registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
                return errorText;
            }

            return smsConsultingElectoralService.processSmsRequest(smsText, senderNumber, registrationStatisticSms);
        } else if (registrationStatisticSms.getFlow() == SmsFlow.ENROLMENT) {
            if (!rangeService.isRangeNameActive(RangeName.CENSUS)) {
                String errorText = translationProperties.getTHIS_IS_NOT_ENROLMENT_PHASE();
                SMSRequest smsRequest = new SMSRequest(senderNumber, errorText);
                ozekiApiCaller.sendMessage(smsRequest);
                registrationStatisticSms.setIsProcessFinished(true);
                registrationStatisticSms.getSteps().add(StepSms.PHASE_ERROR);
                registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
                return errorText;
            }
            return smsEnrolmentService.processSmsRequest(smsText, senderNumber, registrationStatisticSms);
        } else {
            String errorText = translationProperties.getPLEASE_START_AGAIN_TO_ENLIST_I_TRACK_YOUR_PIN_OR_PASSPORT_NUMBER();
            SMSRequest smsRequest = new SMSRequest(senderNumber, errorText);
            ozekiApiCaller.sendMessage(smsRequest);
            registrationStatisticSms.setIsProcessFinished(true);
            registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
            return errorText;
        }
    }

    private boolean initNewSessionDetect(String smsText) {
        return smsText.startsWith("C") || smsText.startsWith("c") || smsText.startsWith("I") || smsText.startsWith("i");
    }

}
