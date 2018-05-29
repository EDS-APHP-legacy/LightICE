/*******************************************************************************
 * Copyright (c) 2014, MD PnP Program
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package drivers.philips.intellivue.attribute;

import java.lang.reflect.InvocationTargetException;

import drivers.philips.intellivue.data.AbsoluteTime;
import drivers.philips.intellivue.data.Altitude;
import drivers.philips.intellivue.data.ApplicationArea;
import drivers.philips.intellivue.data.AttributeId;
import drivers.philips.intellivue.data.ByteArray;
import drivers.philips.intellivue.data.CompoundNumericObservedValue;
import drivers.philips.intellivue.data.DeviceAlarmList;
import drivers.philips.intellivue.data.DeviceAlertCondition;
import drivers.philips.intellivue.data.DisplayResolution;
import drivers.philips.intellivue.data.EnumMessage;
import drivers.philips.intellivue.data.EnumValue;
import drivers.philips.intellivue.data.EnumValueImpl;
import drivers.philips.intellivue.data.HandleId;
import drivers.philips.intellivue.data.IPAddressInformation;
import drivers.philips.intellivue.data.InvokeId;
import drivers.philips.intellivue.data.LineFrequency;
import drivers.philips.intellivue.data.MDSGeneralSystemInfo;
import drivers.philips.intellivue.data.MDSStatus;
import drivers.philips.intellivue.data.MdibObjectSupport;
import drivers.philips.intellivue.data.MeasureMode;
import drivers.philips.intellivue.data.MetricModality;
import drivers.philips.intellivue.data.MetricSpecification;
import drivers.philips.intellivue.data.MetricState;
import drivers.philips.intellivue.data.NomenclatureVersion;
import drivers.philips.intellivue.data.NumericObservedValue;
import drivers.philips.intellivue.data.OIDType;
import drivers.philips.intellivue.data.OperatingMode;
import drivers.philips.intellivue.data.PatientBSAFormula;
import drivers.philips.intellivue.data.PatientDemographicState;
import drivers.philips.intellivue.data.PatientMeasurement;
import drivers.philips.intellivue.data.PatientPacedMode;
import drivers.philips.intellivue.data.PatientSex;
import drivers.philips.intellivue.data.PatientType;
import drivers.philips.intellivue.data.PollProfileExtensions;
import drivers.philips.intellivue.data.PollProfileSupport;
import drivers.philips.intellivue.data.ProductionSpecification;
import drivers.philips.intellivue.data.ProtocolSupport;
import drivers.philips.intellivue.data.RelativeTime;
import drivers.philips.intellivue.data.SampleArrayCompoundObservedValue;
import drivers.philips.intellivue.data.SampleArrayFixedValueSpecification;
import drivers.philips.intellivue.data.SampleArrayObservedValue;
import drivers.philips.intellivue.data.SampleArrayPhysiologicalRange;
import drivers.philips.intellivue.data.SampleArraySpecification;
import drivers.philips.intellivue.data.ScaleAndRangeSpecification;
import drivers.philips.intellivue.data.SimpleColor;
import drivers.philips.intellivue.data.SystemLocalization;
import drivers.philips.intellivue.data.SystemModel;
import drivers.philips.intellivue.data.SystemSpecification;
import drivers.philips.intellivue.data.TextId;
import drivers.philips.intellivue.data.TextIdList;
import drivers.philips.intellivue.data.Type;
import drivers.philips.intellivue.data.UnitCode;
import drivers.philips.intellivue.data.Value;
import drivers.philips.intellivue.data.VisualGrid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeff Plourde
 *
 */
public class AttributeFactory {
    public static final Attribute<PollProfileExtensions> getPollProfileExtensions() {
        return getAttribute(0xF001, PollProfileExtensions.class);
    }

    public static final Attribute<PollProfileSupport> getPollProfileSupport() {
        return getAttribute(0x0001, PollProfileSupport.class);
    }

    public static final Attribute<MdibObjectSupport> getMdibObjectSupport() {
        return getAttribute(0x102, MdibObjectSupport.class);
    }

    public static final <T extends Value> Attribute<T> getAttribute(int oid, Class<T> valueClass) {
        return getAttribute(OIDType.lookup(oid), valueClass);
    }

    public static final <T extends Value> Attribute<T> getAttribute(AttributeId aid, Class<T> valueClass) {
        return getAttribute(aid.asOid(), valueClass);
    }

    public static final <T extends Value> Attribute<T> getAttribute(OIDType oid, Class<T> valueClass) {
        try {
            return new AttributeImpl<>(oid, valueClass.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static final <T extends EnumMessage<T>> Attribute<EnumValue<T>> getEnumAttribute(OIDType oid, Class<T> enumClass) {

        try {
            return new AttributeImpl<EnumValue<T>>(oid, new EnumValueImpl<T>((T) ((Object[]) enumClass.getMethod("values", new Class<?>[0]).invoke(
                    null))[0]));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> valueType(OIDType oid) {
        AttributeId id = AttributeId.valueOf(oid.getType());
        if (null == id) {
            return null;
        } else {
            switch (id) {
            case NOM_ATTR_ID_TYPE:
                return Type.class;
            case NOM_ATTR_ID_HANDLE:
                return HandleId.class;
            case NOM_ATTR_NU_VAL_OBS:
                return NumericObservedValue.class;
            case NOM_ATTR_NU_CMPD_VAL_OBS:
                return CompoundNumericObservedValue.class;
            case NOM_ATTR_TIME_STAMP_ABS:
                return AbsoluteTime.class;
            case NOM_ATTR_TIME_STAMP_REL:
                return RelativeTime.class;
            case NOM_ATTR_ID_LABEL:
                return TextId.class;
            case NOM_ATTR_DISP_RES:
                return DisplayResolution.class;
            case NOM_ATTR_COLOR:
                return SimpleColor.class;
            case NOM_ATTR_METRIC_SPECN:
                return MetricSpecification.class;
            case NOM_ATTR_METRIC_MODALITY:
                return MetricModality.class;
            case NOM_ATTR_SA_SPECN:
                return SampleArraySpecification.class;
            case NOM_ATTR_SA_FIXED_VAL_SPECN:
                return SampleArrayFixedValueSpecification.class;
            case NOM_ATTR_TIME_PD_SAMP:
                return RelativeTime.class;
            case NOM_ATTR_METRIC_STAT:
                return MetricState.class;
            case NOM_ATTR_UNIT_CODE:
                return UnitCode.class;
            case NOM_ATTR_MODE_MSMT:
                return MeasureMode.class;
            case NOM_ATTR_METRIC_INFO_LABEL_STR:
            case NOM_ATTR_ID_LABEL_STRING:
            case NOM_ATTR_ID_BED_LABEL:
            case NOM_ATTR_PT_NAME_GIVEN:
                // case NOM_ATTR_PT_NAME_MIDDLE:
            case NOM_ATTR_PT_NAME_FAMILY:
            case NOM_ATTR_PT_ID:
                // case NOM_ATTR_PT_ENCOUNTER_ID:
            case NOM_ATTR_PT_NOTES1:
            case NOM_ATTR_PT_NOTES2:
                return drivers.philips.intellivue.data.String.class;
            case NOM_ATTR_SCALE_SPECN_I16:
                return ScaleAndRangeSpecification.class;
            case NOM_ATTR_SA_RANGE_PHYS_I16:
                return SampleArrayPhysiologicalRange.class;
            case NOM_ATTR_GRID_VIS_I16:
                return VisualGrid.class;
            case NOM_ATTR_SA_VAL_OBS:
                return SampleArrayObservedValue.class;
            case NOM_ATTR_SA_CMPD_VAL_OBS:
                return SampleArrayCompoundObservedValue.class;
            case NOM_ATTR_SYS_TYPE:
                return Type.class;
            case NOM_ATTR_PCOL_SUPPORT:
                return ProtocolSupport.class;
            case NOM_ATTR_LOCALIZN:
                return SystemLocalization.class;
            case NOM_ATTR_NET_ADDR_INFO:
                return IPAddressInformation.class;
            case NOM_ATTR_SYS_ID:
                return ByteArray.class;
            case NOM_ATTR_ID_ASSOC_NO:
                return InvokeId.class;
            case NOM_ATTR_ID_MODEL:
                return SystemModel.class;
            case NOM_ATTR_NOM_VERS:
                return NomenclatureVersion.class;
            case NOM_ATTR_MODE_OP:
                return OperatingMode.class;
            case NOM_ATTR_AREA_APPL:
                return ApplicationArea.class;
            case NOM_ATTR_LINE_FREQ:
                return LineFrequency.class;
            case NOM_ATTR_TIME_REL:
                return RelativeTime.class;
            case NOM_ATTR_TIME_ABS:
            case NOM_ATTR_PT_DOB:
                return AbsoluteTime.class;
            case NOM_ATTR_ALTITUDE:
                return Altitude.class;
            case NOM_ATTR_VMS_MDS_STAT:
                return MDSStatus.class;
            case NOM_ATTR_MDS_GEN_INFO:
                return MDSGeneralSystemInfo.class;
            case NOM_ATTR_ID_PROD_SPECN:
                return ProductionSpecification.class;
            case NOM_ATTR_TIME_PD_POLL:
                return RelativeTime.class;
            case NOM_ATTR_POLL_RTSA_PRIO_LIST:
                // Received MDS Set Priority List Result Wave.
            case NOM_ATTR_POLL_NU_PRIO_LIST:
                // Received MDS Set Priority List Result Numerics.
            case NOM_ATTR_POLL_OBJ_PRIO_NUM:
                return TextIdList.class;
            case NOM_ATTR_PT_DEMOG_ST:
                return PatientDemographicState.class;
            case NOM_ATTR_PT_TYPE:
                return PatientType.class;
            case NOM_ATTR_PT_PACED_MODE:
                return PatientPacedMode.class;
            case NOM_ATTR_PT_SEX:
                return PatientSex.class;
            case NOM_ATTR_PT_WEIGHT:
            case NOM_ATTR_PT_AGE:
            case NOM_ATTR_PT_HEIGHT:
            case NOM_ATTR_PT_BSA:
                return PatientMeasurement.class;
            case NOM_ATTR_PT_BSA_FORMULA:
                return PatientBSAFormula.class;
            case NOM_ATTR_SYS_SPECN:
                return SystemSpecification.class;
            case NOM_ATTR_DEV_AL_COND:
                return DeviceAlertCondition.class;
            case NOM_ATTR_AL_MON_T_AL_LIST:
            case NOM_ATTR_AL_MON_P_AL_LIST:
                return DeviceAlarmList.class;
            default:
                return null;
            }
        }

    }

    private static final Logger log = LoggerFactory.getLogger(AttributeFactory.class);

    @SuppressWarnings("unchecked")
    public static final <T extends EnumMessage<T>> Attribute<?> getAttribute(OIDType oid) {
        Class<?> valueType = valueType(oid);
        if (null == valueType) {
            return null;
        } else if (valueType.isEnum()) {
            return getEnumAttribute(oid, (Class<T>) valueType);
        } else {
            return getAttribute(oid, ((Class<Value>) valueType(oid)));
        }
    }
}
