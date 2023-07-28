package net.sunshow.toolkit.core.base.enums.converter;

import jakarta.persistence.Converter;
import net.sunshow.toolkit.core.base.enums.YesNoStatus;

@Converter(autoApply = true)
public class YesNoStatusConverter extends BaseEnumConverter<YesNoStatus> {
}
