package net.sunshow.toolkit.core.base.enums.converter;

import jakarta.persistence.Converter;
import net.sunshow.toolkit.core.base.enums.EnableStatus;

@Converter(autoApply = true)
public class EnableStatusConverter extends BaseEnumConverter<EnableStatus> {
}
