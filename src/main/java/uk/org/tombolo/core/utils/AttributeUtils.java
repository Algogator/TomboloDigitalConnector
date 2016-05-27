package uk.org.tombolo.core.utils;

import java.util.*;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.execution.spec.AttributeSpecification;
import uk.org.tombolo.execution.spec.DatasetSpecification;

public class AttributeUtils {

	public static Attribute getTestAttribute(){
		return HibernateUtil.withSession(session -> {
			Criteria criteria = session.createCriteria(Attribute.class);
			Map<String, Object> restrictions = new HashMap<String, Object>();
			restrictions.put("provider", ProviderUtils.getTestProvider());
			restrictions.put("label", "testAttribute");
			return (Attribute) criteria.add(Restrictions.allEq(restrictions)).uniqueResult();
		});
	}
	
	public static void save(List<Attribute> attributes){
		HibernateUtil.withSession(session -> {
			session.beginTransaction();
			for (Attribute attribute : attributes) {
				// FIXME: This might be inefficient if we are updating the attribute over and over again without actually changing it
				Criteria criteria = session.createCriteria(Attribute.class);
				Map<String, Object> restrictions = new HashMap<String, Object>();
				restrictions.put("provider", attribute.getProvider());
				restrictions.put("label", attribute.getLabel());
				Attribute savedAttribute = (Attribute) criteria.add(Restrictions.allEq(restrictions)).uniqueResult();
				if (savedAttribute == null) {
					Integer id = (Integer) session.save(attribute);
					attribute.setId(id);
				} else {
					attribute.setId(savedAttribute.getId());
					savedAttribute.setProvider(attribute.getProvider());
					savedAttribute.setLabel(attribute.getLabel());
					savedAttribute.setName(attribute.getName());
					savedAttribute.setDescription(attribute.getDescription());
					session.save(savedAttribute);
				}
			}
			session.getTransaction().commit();
		});
	}

	public static void save(Attribute attribute) {
		save(Arrays.asList(attribute));
	}

	public static Attribute getByProviderAndLabel(Provider provider, String label){
		return HibernateUtil.withSession(session -> {
			Criteria criteria = session.createCriteria(Attribute.class);
			Map<String, Object> restrictions = new HashMap<String, Object>();
			restrictions.put("provider", provider);
			restrictions.put("label", label);
			Attribute attribute = (Attribute) criteria.add(Restrictions.allEq(restrictions)).uniqueResult();
			return attribute;
		});
	}

	public static Attribute getByProviderAndLabel(String providerLabel, String attributeLabel) {
		return HibernateUtil.withSession(session -> {
			Criteria criteria = session.createCriteria(Attribute.class);
			Map<String, Object> restrictions = new HashMap<String, Object>();
			restrictions.put("provider.label", providerLabel);
			restrictions.put("label", attributeLabel);
			Attribute attribute = (Attribute) criteria.add(Restrictions.allEq(restrictions)).uniqueResult();
			return attribute;
		});
	}

	public static List<Attribute> getAttributeBySpecification(DatasetSpecification datasetSpecification) {
		List<Attribute> list = new ArrayList<>();

		List<AttributeSpecification> attributeSpecs = datasetSpecification.getAttributeSpecification();
		for (AttributeSpecification attributeSpec : attributeSpecs) {
			Provider provider = ProviderUtils.getByLabel(attributeSpec.getProviderLabel());
			Attribute attribute = AttributeUtils.getByProviderAndLabel(provider, attributeSpec.getAttributeLabel());

			if (null != attribute) {
				list.add(attribute);
			}
		}

		return list;
	}
	
}
