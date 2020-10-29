package com.accenture.javamos.controller;

import com.accenture.javamos.model.FlightWithNoReturnDateResponse;
import com.accenture.javamos.model.FlightWithReturnDateResponse;
import com.accenture.javamos.service.AirlineService;
import com.accenture.javamos.service.FlightService;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.Airline;
import com.amadeus.resources.FlightOfferSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/flight")
public class FlightController {

    @Autowired
    FlightService flightService;

    @Autowired
    AirlineService airlineService;

    @GetMapping(path = "/search", params = {"from", "to", "departure", "adults"})
    public ResponseEntity<Object> flightOfferSearchWithNoReturnDate(@RequestParam("from") String fromIataCode,
                                                    @RequestParam("to") String toIataCode,
                                                    @RequestParam("departure") String departureDate,
                                                    @RequestParam("adults") Integer adults) throws ResponseException {

        try {
            FlightOfferSearch[] flightOffersSearches = flightService.flightOfferSearchWithNoReturnDate(fromIataCode, toIataCode, departureDate, adults);
            Airline[] airlines = airlineService.getAllAirline();
            List<FlightWithNoReturnDateResponse> flightWithNoReturnDateResponse = new ArrayList<FlightWithNoReturnDateResponse>();

            for (FlightOfferSearch f : flightOffersSearches) {
                int intineraryLength = f.getItineraries().length - 1;
                int segmentsLength = f.getItineraries()[intineraryLength].getSegments().length - 1;

                List<String> airlinesList = new ArrayList<String>();
                for (Airline a : airlines)
                    for (String s : f.getValidatingAirlineCodes())
                        if (a.getIataCode().equals(s))
                            airlinesList.add(a.getCommonName());

                flightWithNoReturnDateResponse.add(new FlightWithNoReturnDateResponse(
                        f.getItineraries()[0].getSegments()[0].getDeparture().getIataCode(),
                        f.getItineraries()[intineraryLength].getSegments()[segmentsLength].getArrival().getIataCode(),
                        (Date) Date.from(Instant.parse(f.getItineraries()[0].getSegments()[0].getDeparture().getAt() + "Z")),
                        airlinesList,
                        f.isOneWay(),
                        f.getNumberOfBookableSeats(),
                        f.getPrice().getCurrency(),
                        f.getPrice().getGrandTotal()
                ));
            }
            return new ResponseEntity<>(flightWithNoReturnDateResponse, HttpStatus.OK);
        } catch (ResponseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("{\"status_code\": " + HttpStatus.INTERNAL_SERVER_ERROR.value() + ",\"message\": \"" + HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
