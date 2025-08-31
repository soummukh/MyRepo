package uk.co.gamma.address.model.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.gamma.address.model.Address;
import uk.co.gamma.address.model.db.entity.AddressEntity;

@Mapper(componentModel = "spring")
public interface AddressMapper implements JpaRepository<List> {

    AddressEntity modelToEntity(Address address);

    Address entityToModel(AddressEntity address);

    List<Address> entityToModel(Iterable<AddressEntity> address);

}
