package uk.org.tombolo.transformer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

public class SumFractionTransformer extends AbstractTransformer implements Transformer {


	/**
	 * A transformer for summing the a list of attributes and then divide by the value of an attribute.
	 * In case multiple values exist for a geogrphy/attribute then the latest is chosen.
	 *
	 * @param geographies is the list of Geographies over which to transform the data
	 * @param inputAttributes is a list of n attributes. The first n-1 will be summed and divided by the nth.
	 * @param outputAttribute is the attribute that for which the final value will be generated.
     * @return A list of TimedValue objects for different Geographies in the input, the output attribute and latest timestamp.
     */
	@Override
	public List<TimedValue> transform(List<Geography> geographies, List<Attribute> inputAttributes, Attribute outputAttribute) {
		List<TimedValue> values = new ArrayList<TimedValue>();

		// FIXME: extract
		ProviderUtils.save(outputAttribute.getProvider());
		AttributeUtils.save(outputAttribute);
		AttributeUtils.save(inputAttributes);

		for (Geography geography : geographies) {
			LocalDateTime latestTime = LocalDateTime.MIN;
			double value = 0d;

			// Sum attributes 1 ... n-1
			for (int i = 0; i < inputAttributes.size() - 1; i++) {
				// Process for the first n-1 values and sum them
				Attribute attribute = inputAttributes.get(i);
				Optional<TimedValue> optionalTimedValue = timedValueUtils.getLatestByGeographyAndAttribute(geography, attribute);
				if (optionalTimedValue.isPresent()) {
					value += optionalTimedValue.get().getValue();
					if (optionalTimedValue.get().getId().getTimestamp().isAfter(latestTime))
						// The timestamp is later than any timestamp seen before
						latestTime = optionalTimedValue.get().getId().getTimestamp();
				}
			}

			// Divide by nth attribute
			Attribute attribute = inputAttributes.get(inputAttributes.size()-1);
			Optional<TimedValue> timedValueOptional = timedValueUtils.getLatestByGeographyAndAttribute(geography, attribute);
			if (timedValueOptional.isPresent()) {
				value /= timedValueOptional.get().getValue();
				if (timedValueOptional.get().getId().getTimestamp().isAfter(latestTime))
					// The timestamp is later that any timestamp seen before
					latestTime = timedValueOptional.get().getId().getTimestamp();
			}

			// Create the transformed output value
			values.add(new TimedValue(geography, outputAttribute, latestTime, value));
		}

		this.timedValueUtils.save(values);
		return values;
	}

}
