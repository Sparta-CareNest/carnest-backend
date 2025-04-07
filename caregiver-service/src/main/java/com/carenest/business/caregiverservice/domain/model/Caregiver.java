package com.carenest.business.caregiverservice.domain.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Caregiver {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID Id;
}
