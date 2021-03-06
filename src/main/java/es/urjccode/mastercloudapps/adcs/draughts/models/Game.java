package es.urjccode.mastercloudapps.adcs.draughts.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {

	private Board board;
	private Turn turn;
    private Coordinate remove;

    public Coordinate getRemove() {
        return remove;
    }

    public void setRemove(Coordinate remove) {
        this.remove = remove;
    }

	Game(Board board) {
		this.turn = new Turn();
		this.board = board;
	}

	public Game() {
		this(new Board());
		this.reset();
	}

	public void reset() {
		for (int i = 0; i < Coordinate.getDimension(); i++)
			for (int j = 0; j < Coordinate.getDimension(); j++) {
				Coordinate coordinate = new Coordinate(i, j);
				Color color = Color.getInitialColor(coordinate);
				Piece piece = null;
				if (color != null)
					piece = new Pawn(color);
				this.board.put(coordinate, piece);
			}
		if (this.turn.getColor() != Color.WHITE)
			this.turn.change();
	}

	public Error move(Coordinate... coordinates) {
		Error error;
		List<Coordinate> removedCoordinates = new ArrayList<>();
		int pair = 0;
		do {
			error = this.isCorrectPairMove(pair, coordinates);
			if (error == null) {
				this.pairMove(removedCoordinates, pair, coordinates);
				pair++;
			}
		} while (pair < coordinates.length - 1 && error == null);
		error = this.isCorrectGlobalMove(error, removedCoordinates, coordinates);
		if (error == null)
			this.turn.change();
		else
			this.unMovesUntilPair(removedCoordinates, pair, coordinates);
		return error;
	}

	private Error isCorrectPairMove(int pair, Coordinate... coordinates) {
		assert coordinates[pair] != null;
		assert coordinates[pair + 1] != null;
		if (board.isEmpty(coordinates[pair]))
			return Error.EMPTY_ORIGIN;
		if (this.turn.getOppositeColor() == this.board.getColor(coordinates[pair]))
			return Error.OPPOSITE_PIECE;
		if (!this.board.isEmpty(coordinates[pair + 1]))
			return Error.NOT_EMPTY_TARGET;
		List<Piece> betweenDiagonalPieces =
			this.board.getBetweenDiagonalPieces(coordinates[pair], coordinates[pair + 1]);
		return this.board.getPiece(coordinates[pair]).isCorrectMovement(betweenDiagonalPieces, pair, coordinates);
	}

	private void pairMove(List<Coordinate> removedCoordinates, int pair, Coordinate... coordinates) {
		Coordinate forRemoving = this.getBetweenDiagonalPiece(pair, coordinates);
		if (forRemoving != null) {
			removedCoordinates.add(0, forRemoving);
			this.board.remove(forRemoving);
		}else{
            Coordinate betweenDiagonalPiece = this.getBetweenDiagonalPiece(coordinates[pair]);
            if(betweenDiagonalPiece != null){
                this.randomRemove(pair,coordinates);
            }
        }
		this.board.move(coordinates[pair], coordinates[pair + 1]);
	}

    private void randomRemove(int pair,Coordinate... coordinates){

        Piece piece;
        boolean b = false;
        do {
            this.setRemove(new Coordinate(this.getRandom(7),this.getRandom(7)));
            piece = this.getPiece(getRemove());

            if (piece != null && this.turn.getOppositeColor() != this.board.getColor(getRemove())) {
                if(getRemove().getRow() != coordinates[pair].getRow() && getRemove().getColumn() != coordinates[pair].getColumn()){
                    b = true;
                    System.out.println(getRemove());
                    this.board.remove(getRemove());
                }
            }
        } while (!b);
    }

    private int getRandom(int n){
        Random ran = new Random();
        int random = ran.nextInt(n);
        return random;
    }

	private Coordinate getBetweenDiagonalPiece(int pair, Coordinate... coordinates) {
		assert coordinates[pair].isOnDiagonal(coordinates[pair + 1]);
		List<Coordinate> betweenCoordinates = coordinates[pair].getBetweenDiagonalCoordinates(coordinates[pair + 1]);
		if (betweenCoordinates.isEmpty())
			return null;
		for (Coordinate coordinate : betweenCoordinates) {
			if (this.getPiece(coordinate) != null)
				return coordinate;
		}
		return null;
	}

    private Coordinate getBetweenDiagonalPiece(Coordinate coordinate,Coordinate tpCoordinate) {

        if(this.getPiece(tpCoordinate) != null){
            return null;
        }
        List<Coordinate> betweenCoordinates = coordinate.getBetweenDiagonalCoordinates(tpCoordinate);
        if (betweenCoordinates.isEmpty())
            return null;
        for (Coordinate coordinate1 : betweenCoordinates) {
            if (this.getPiece(coordinate1) != null)
                return coordinate1;
        }
        return null;
    }

    private Coordinate getBetweenDiagonalPiece(Coordinate coordinate) {
        Coordinate tpCoordinate;

        if(coordinate.getRow() >= 2 && coordinate.getRow() <= 5){
            Color color = this.board.getColor(coordinate);
            color.toString();
            int toRow = 2;
            if("WHITE".equals(color.toString())){
                toRow = -2;
            }

            if(coordinate.getColumn() >= 2){
                tpCoordinate = new Coordinate(coordinate.getRow()+toRow,coordinate.getColumn()-2);
                Coordinate betweenDiagonalPiece = getBetweenDiagonalPiece(coordinate, tpCoordinate);
                if(betweenDiagonalPiece != null && this.board.getColor(betweenDiagonalPiece) != color){
                    return betweenDiagonalPiece;
                }
            }

            if(coordinate.getColumn() <= 5) {
                tpCoordinate = new Coordinate(coordinate.getRow() + toRow, coordinate.getColumn() + 2);
                Coordinate betweenDiagonalPiece = getBetweenDiagonalPiece(coordinate, tpCoordinate);
                if(betweenDiagonalPiece != null && this.board.getColor(betweenDiagonalPiece) != color){
                    return betweenDiagonalPiece;
                }
            }
        }
        return null;
    }

	private Error isCorrectGlobalMove(Error error, List<Coordinate> removedCoordinates, Coordinate... coordinates){
		if (error != null)
			return error;
		if (coordinates.length > 2 && coordinates.length > removedCoordinates.size() + 1)
			return Error.TOO_MUCH_JUMPS;
		return null;
	}

	private void unMovesUntilPair(List<Coordinate> removedCoordinates, int pair, Coordinate... coordinates) {
		for (int j = pair; j > 0; j--)
			this.board.move(coordinates[j], coordinates[j - 1]);
		for (Coordinate removedPiece : removedCoordinates)
			this.board.put(removedPiece, new Pawn(this.getOppositeTurnColor()));
	}

	public boolean isBlocked() {
		for (Coordinate coordinate : this.getCoordinatesWithActualColor())
			if (!this.isBlocked(coordinate))
				return false;
		return true;
	}

	private List<Coordinate> getCoordinatesWithActualColor() {
		List<Coordinate> coordinates = new ArrayList<>();
		for (int i = 0; i < this.getDimension(); i++) {
			for (int j = 0; j < this.getDimension(); j++) {
				Coordinate coordinate = new Coordinate(i, j);
				Piece piece = this.getPiece(coordinate);
				if (piece != null && piece.getColor() == this.getTurnColor())
					coordinates.add(coordinate);
			}
		}
		return coordinates;
	}

	private boolean isBlocked(Coordinate coordinate) {
		for (int i = 1; i <= 2; i++)
			for (Coordinate target : coordinate.getDiagonalCoordinates(i))
				if (this.isCorrectPairMove(0, coordinate, target) == null)
					return false;
		return true;
	}

	public void cancel() {
		for (Coordinate coordinate : this.getCoordinatesWithActualColor())
			this.board.remove(coordinate);
		this.turn.change();
	}

	public Color getColor(Coordinate coordinate) {
		assert coordinate != null;
		return this.board.getColor(coordinate);
	}

	public Color getTurnColor() {
		return this.turn.getColor();
	}

	private Color getOppositeTurnColor() {
		return this.turn.getOppositeColor();
	}

	public Piece getPiece(Coordinate coordinate) {
		assert coordinate != null;
		return this.board.getPiece(coordinate);
	}

	public int getDimension() {
		return Coordinate.getDimension();
	}

	@Override
	public String toString() {
		return this.board + "\n" + this.turn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((board == null) ? 0 : board.hashCode());
		result = prime * result + ((turn == null) ? 0 : turn.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Game other = (Game) obj;
		if (board == null) {
			if (other.board != null)
				return false;
		} else if (!board.equals(other.board))
			return false;
		if (turn == null) {
            return other.turn == null;
		} else return turn.equals(other.turn);
    }
}
