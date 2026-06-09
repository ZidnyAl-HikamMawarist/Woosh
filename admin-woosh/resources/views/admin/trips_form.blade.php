@extends('layouts.admin')

@section('title', isset($trip) ? 'Edit Jadwal Kereta' : 'Tambah Jadwal Kereta')

@section('content')
    <div class="card" style="max-width: 600px; margin: 0 auto;">
        <div class="card-header">
            <h3 class="card-title">{{ isset($trip) ? 'Edit Jadwal' : 'Tambah Jadwal Baru' }}</h3>
            <a href="{{ route('admin.trips') }}" class="btn btn-secondary">Kembali</a>
        </div>

        @if ($errors->any())
            <div style="background-color: #fee2e2; color: #991b1b; padding: 16px; border-radius: 8px; margin-bottom: 20px;">
                <ul style="margin-left: 20px;">
                    @foreach ($errors->all() as $error)
                        <li>{{ $error }}</li>
                    @endforeach
                </ul>
            </div>
        @endif

        <form action="{{ isset($trip) ? route('admin.trips.update', $trip->trip_id) : route('admin.trips.store') }}" method="POST">
            @csrf
            @if(isset($trip))
                @method('PUT')
            @endif

            @if(!isset($trip))
            <div class="form-group">
                <label>Trip ID</label>
                <input type="text" name="trip_id" class="form-control" value="{{ old('trip_id') }}" placeholder="T1001" required>
            </div>
            @endif

            <div class="form-group">
                <label>Nama Kereta</label>
                <input type="text" name="train_name" class="form-control" value="{{ old('train_name', $trip->train_name ?? '') }}" placeholder="Woosh G100" required>
            </div>

            <div class="form-group">
                <label>Kelas Kereta</label>
                <select name="train_class" class="form-control" required>
                    <option value="Premium Economy" {{ old('train_class', $trip->train_class ?? '') == 'Premium Economy' ? 'selected' : '' }}>Premium Economy</option>
                    <option value="Business" {{ old('train_class', $trip->train_class ?? '') == 'Business' ? 'selected' : '' }}>Business</option>
                    <option value="First Class" {{ old('train_class', $trip->train_class ?? '') == 'First Class' ? 'selected' : '' }}>First Class</option>
                </select>
            </div>

            <div class="form-group">
                <label>Waktu Keberangkatan</label>
                <input type="datetime-local" name="departure_time" class="form-control" value="{{ old('departure_time', isset($trip) ? date('Y-m-d\TH:i', strtotime($trip->departure_time)) : '') }}" required>
            </div>

            <div class="form-group">
                <label>Waktu Kedatangan</label>
                <input type="datetime-local" name="arrival_time" class="form-control" value="{{ old('arrival_time', isset($trip) ? date('Y-m-d\TH:i', strtotime($trip->arrival_time)) : '') }}" required>
            </div>

            <div class="form-group">
                <label>Harga Dasar (Rp)</label>
                <input type="number" name="base_price" class="form-control" value="{{ old('base_price', isset($trip) ? (int)$trip->base_price : '') }}" required>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%;">{{ isset($trip) ? 'Simpan Perubahan' : 'Tambah Jadwal' }}</button>
        </form>
    </div>
@endsection
