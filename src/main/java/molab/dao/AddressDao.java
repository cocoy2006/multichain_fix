package molab.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import molab.entity.Address;

@Repository
public interface AddressDao extends JpaRepository<Address, Integer> {

}
