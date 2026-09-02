package org.duckdns.whocaresleft.presenter;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.duckdns.whocaresleft.exception.DepartmentNotFoundException;
import org.duckdns.whocaresleft.exception.DoctorNotFoundException;
import org.duckdns.whocaresleft.exception.OverlappedShiftException;
import org.duckdns.whocaresleft.exception.ShiftNotFoundException;
import org.duckdns.whocaresleft.model.Shift;
import org.duckdns.whocaresleft.repository.ShiftRepository;
import org.duckdns.whocaresleft.transaction.TransactionManager;
import org.duckdns.whocaresleft.view.ShiftView;

public class ShiftPresenter {
    
    private static final Logger LOGGER = LogManager.getLogger(ShiftPresenter.class);
    
    private TransactionManager transactionManager;
    private ShiftView view;
    
    public ShiftPresenter(TransactionManager transactionManager, ShiftView view) {
        this.transactionManager = transactionManager;
        this.view = view;
    }
    
    public synchronized void allShifts() {
        List<Shift> shifts = transactionManager.doInTransaction(repositoryProvider -> {
            ShiftRepository repository = repositoryProvider.getShiftRepository();
            return repository.findAll();
        });
        LOGGER.debug("Retrieved {} shifts from repository.", shifts.size());
        view.showAllShifts(shifts);
    }
    
    public synchronized void addShift(Shift shift) {
        try {
            transactionManager.doInTransaction(repositoryProvider -> {
                if (repositoryProvider.getDoctorRepository().findById(shift.getDoctorId()) == null)
                    throw new DoctorNotFoundException(shift.getDoctorId());
                if (repositoryProvider.getDepartmentRepository().findById(shift.getDepartmentId()) == null) 
                    throw new DepartmentNotFoundException(shift.getDepartmentId());
                
                ShiftRepository repository = repositoryProvider.getShiftRepository();
                Optional<Shift> conflicting
                    = repository.findByDoctorId(shift.getDoctorId())
                        .stream()
                        .filter(s -> s.overlaps(shift))
                        .findFirst();
                
                if (conflicting.isPresent())
                    throw new OverlappedShiftException(conflicting.get(), shift);
                
                repository.save(shift);
                return null;
            });
            LOGGER.debug("Shift {} was saved to repository", shift);
            view.shiftAdded(shift);
            
        } catch (OverlappedShiftException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showErrorOverlappedShift(e.getConflictingShift(), e.getOverlappedShift());
            
        } catch (DoctorNotFoundException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showErrorDoctorNotFound(e.getDoctorId());
            
        } catch (DepartmentNotFoundException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showErrorDepartmentNotFound(e.getDepartmentId());
        }
    }
    
    public synchronized void removeShift(Shift shift) {
        try {
            transactionManager.doInTransaction(repositoryProvider -> {
                ShiftRepository repository = repositoryProvider.getShiftRepository();
                repository.delete(shift);
                return null;
            });
            LOGGER.debug("Shift {} was deleted from repository", shift);
            view.shiftRemoved(shift);
        } catch (ShiftNotFoundException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showErrorShiftNotFound(e.getShift());
        }
    }
    
    public synchronized void updateShift(Shift oldShift, Shift newShift) {
        try {
            transactionManager.doInTransaction(repositoryProvider -> {
                if (repositoryProvider.getDoctorRepository().findById(newShift.getDoctorId()) == null)
                    throw new DoctorNotFoundException(newShift.getDoctorId());
                if (repositoryProvider.getDepartmentRepository().findById(newShift.getDepartmentId()) == null) 
                    throw new DepartmentNotFoundException(newShift.getDepartmentId());
                
                ShiftRepository repository = repositoryProvider.getShiftRepository();
                
                Optional<Shift> conflicting =
                    repository.findByDoctorId(newShift.getDoctorId())
                        .stream()
                        .filter(s -> s.overlaps(newShift))
                        .filter(s -> !s.equals(oldShift))
                        .findFirst();
                
                if (conflicting.isPresent())
                    throw new OverlappedShiftException(conflicting.get(), newShift);
                
                repository.update(oldShift, newShift);
                return null;
            });
            LOGGER.debug("Shift {} was updated into {}", oldShift, newShift);
            view.shiftUpdated(oldShift, newShift);
            
        } catch (ShiftNotFoundException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showErrorShiftNotFound(e.getShift());
            
        } catch (OverlappedShiftException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showErrorOverlappedShift(e.getConflictingShift(), e.getOverlappedShift());
            
        } catch (DoctorNotFoundException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showErrorDoctorNotFound(e.getDoctorId());
            
        } catch (DepartmentNotFoundException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showErrorDepartmentNotFound(e.getDepartmentId());
        }
    }
}
