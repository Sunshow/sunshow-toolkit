package net.sunshow.toolkit.core.base.enums.converter;

import net.sunshow.toolkit.core.base.enums.YesNoStatus;

import javax.persistence.Converter;

@Converter(autoApply = true)
public class YesNoStatusConverter extends BaseEnumConverter<YesNoStatus> {
}
