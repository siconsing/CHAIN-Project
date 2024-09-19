package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.Form;

@Repository
public interface FormRepository extends JpaRepository<Form, String> {
}
