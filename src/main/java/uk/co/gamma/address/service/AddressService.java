package uk.co.gamma.address.service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.gamma.address.enums.DeliveryMode;
import uk.co.gamma.address.exception.AddressNotFoundException;
import uk.co.gamma.address.model.Address;
import uk.co.gamma.address.model.Zone;
import uk.co.gamma.address.model.db.entity.AddressEntity;
import uk.co.gamma.address.model.db.repository.AddressRepository;
import uk.co.gamma.address.model.mapper.AddressMapper;

/**
 * Address service is a Component class that returns  {@link Address}.
 */
@Component
public class AddressService {

    private static final Logger logger = LoggerFactory.getLogger(AddressService.class);

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final BlackListService blackListService;



    /**
     * Constructor.

     * @param addressRepository  {@link AddressRepository}.
     * @param addressMapper  {@link AddressMapper}
     */
    @Autowired
    AddressService(AddressRepository addressRepository, AddressMapper addressMapper,BlackListService blackListService ) {
        this.addressRepository = addressRepository;
        this.addressMapper = addressMapper;
        this.blackListService=blackListService;
    }

    /**
     * getAll get all the addresses of the system.

     * @return List  {@link Address} . Empty if none found.
     */
    public List<Address> getAll(DeliveryMode deliveryMode) {
        try {
        List<Address> addresses = addressMapper.entityToModel(addressRepository.findAll());

        if(deliveryMode.toString().equals(DeliveryMode.FOOT.toString())){
            return addresses; // return all for foot delivery
        }
        List<String> blacklistedPostcodes = null;
            blacklistedPostcodes = blackListService.getAll().stream().map(Zone::getPostCode).toList();
            List<String> finalBlacklistedPostcodes = blacklistedPostcodes;
            return addresses.stream().filter(address -> !finalBlacklistedPostcodes.contains(address.postcode())).toList();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public List<Address> getByPostcode(String postcode, DeliveryMode mode) {
        List<String> blacklisted = null;
        try {
            blacklisted = blackListedPostCodes(postcode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!blacklisted.isEmpty() && mode.toString().equals("DRIVE")) {
            System.out.println("The postcode " + postcode + " is blacklisted. Skipping address retrieval.");
            return Collections.emptyList();
        } else {
            return addressMapper.entityToModel(addressRepository.findByPostcode(postcode));
    }}

    public List<String> blackListedPostCodes(String postCode) throws IOException, InterruptedException {

        List<Zone> zonesPost= blackListService.getAll();

        List<String> result = zonesPost.stream()
                .filter(e -> e.getPostCode().equals(postCode))
                .map(Zone::getPostCode)
                .limit(1)   // Ensure that we only pick up one zone even if multiple match.
                .collect(Collectors.toList());

        // Print the matching zone if found
        result.forEach(code -> System.out.println("Matching zone: " + code));

        return result;

    }

    /**
     * findById find an address by Id.

     * @param id to search on.

     * @return  {@link Address} Optional.
     */
    public Optional<Address> getById(Integer id) {
        return addressRepository.findById(id).map(addressMapper::entityToModel);
    }

    /**
     * create Address and save to db.

     * @param address   {@link Address} to save

     * @return  {@link Address}
     */
    public Address create(Address address) {
        logger.info("Adding new address: {}", address);
        return save(addressMapper.modelToEntity(address));
    }

    /**
     * update an Address.

     * @param id of Address

     * @param address  {@link Address}

     * @return {@link Address}
     */
    public Address update(Integer id, Address address) {
        return addressRepository.findById(id).map(addressEntity -> {
            logger.info("Updating existing address {}: {}", id, address);
            addressEntity.setBuilding(address.building());
            addressEntity.setStreet(address.street());
            addressEntity.setTown(address.town());
            addressEntity.setPostcode(address.postcode());
            return save(addressEntity);
        }).orElseThrow(() -> new AddressNotFoundException(id));

    }

    /**
     * delete an address by id.

     * @param id of Address to delete
     */
    public void delete(Integer id) {
        if (!addressRepository.existsById(id)) {
            throw new AddressNotFoundException(id);
        }
        logger.info("Deleting address {}", id);
        addressRepository.deleteById(id);
    }

    /**
     * save an  {@link Address}.

     * @param addressEntity  {@link AddressEntity}

     * @return  {@link Address}
     */
    private Address save(AddressEntity addressEntity) {
        return addressMapper.entityToModel(addressRepository.save(addressEntity));
    }
}
