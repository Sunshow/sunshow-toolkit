package net.sunshow.toolkit.core.base.enums.converter;

import net.sunshow.toolkit.core.base.enums.EnableStatus;

import javax.persistence.Converter;

@Converter(autoApply = true)
public class EnableStatusConverter extends BaseEnumConverter<EnableStatus> {
}
